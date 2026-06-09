using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Options;

namespace Habitus.Stats;

public sealed class StatsService
{
    private readonly IWebHostEnvironment environment;
    private readonly StatsOptions options;
    private readonly TokenReader tokenReader;

    public StatsService(IOptions<StatsOptions> options, TokenReader tokenReader, IWebHostEnvironment environment)
    {
        this.options = options.Value;
        this.tokenReader = tokenReader;
        this.environment = environment;
    }

    public async Task<EvolutionStatsResponse> GetEvolutionAsync(
        string authorizationHeader,
        int days,
        CancellationToken cancellationToken)
    {
        if (days < 1 || days > 365)
        {
            throw new ArgumentException("O periodo deve ficar entre 1 e 365 dias.", nameof(days));
        }

        var userId = tokenReader.ReadUserId(authorizationHeader);
        var endDate = DateOnly.FromDateTime(DateTime.Today);
        var startDate = endDate.AddDays(-(days - 1));

        await using var connection = new SqliteConnection(CreateConnectionString());
        await connection.OpenAsync(cancellationToken);

        if (!await UserExistsAsync(connection, userId, cancellationToken))
        {
            throw new UnauthorizedAccessException("Usuario nao encontrado");
        }

        var habits = await LoadActiveHabitsAsync(connection, userId, cancellationToken);
        var reminderCounts = await LoadReminderCountsAsync(connection, userId, cancellationToken);
        var frequencyDays = await LoadFrequencyDaysAsync(connection, userId, cancellationToken);
        var entries = await LoadEntriesAsync(connection, userId, startDate, endDate, cancellationToken);
        var plans = await LoadPlansAsync(connection, userId, startDate, endDate, cancellationToken);
        var completedTimes = await LoadCompletedTimesAsync(connection, userId, startDate, endDate, cancellationToken);

        var habitsById = habits.ToDictionary((habit) => habit.Id);
        var plansByEntryId = plans
            .GroupBy((plan) => plan.EntryId)
            .ToDictionary((group) => group.Key, (group) => group.ToList());

        var dailySeries = new List<DailyStats>();
        for (var date = startDate; date <= endDate; date = date.AddDays(1))
        {
            var dayTotals = entries.TryGetValue(date, out var entryId)
                ? CalculateManualDay(entryId, plansByEntryId, habitsById, reminderCounts, completedTimes)
                : CalculateAutomaticDay(date, habits, reminderCounts, frequencyDays);

            dailySeries.Add(new DailyStats(
                date,
                dayTotals.PlannedUnits,
                dayTotals.CompletedUnits,
                Rate(dayTotals.CompletedUnits, dayTotals.PlannedUnits)
            ));
        }

        var plannedUnits = dailySeries.Sum((day) => day.PlannedUnits);
        var completedUnits = dailySeries.Sum((day) => day.CompletedUnits);
        var summary = new EvolutionSummary(
            habits.Count,
            plannedUnits,
            completedUnits,
            Rate(completedUnits, plannedUnits),
            CalculateCurrentStreak(dailySeries),
            CalculateBestStreak(dailySeries)
        );

        return new EvolutionStatsResponse(
            new StatsPeriod(startDate, endDate, days),
            summary,
            dailySeries
        );
    }

    private string CreateConnectionString()
    {
        var configuredPath = Environment.GetEnvironmentVariable("HABITUS_STATS_DATABASE_PATH");
        if (string.IsNullOrWhiteSpace(configuredPath))
        {
            configuredPath = options.DatabasePath;
        }

        var databasePath = Path.GetFullPath(configuredPath, environment.ContentRootPath);
        return new SqliteConnectionStringBuilder
        {
            DataSource = databasePath,
            Mode = SqliteOpenMode.ReadOnly
        }.ToString();
    }

    private static async Task<bool> UserExistsAsync(SqliteConnection connection, long userId, CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = "select 1 from users where id = $userId limit 1";
        command.Parameters.AddWithValue("$userId", userId);

        var value = await command.ExecuteScalarAsync(cancellationToken);
        return value is not null;
    }

    private static async Task<List<HabitRow>> LoadActiveHabitsAsync(
        SqliteConnection connection,
        long userId,
        CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = """
            select id, frequency_type
            from habits
            where user_id = $userId
              and active = 1
            order by created_at asc
            """;
        command.Parameters.AddWithValue("$userId", userId);

        var habits = new List<HabitRow>();
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            habits.Add(new HabitRow(
                reader.GetInt64(0),
                reader.IsDBNull(1) ? "EVERY_DAY" : reader.GetString(1)
            ));
        }

        return habits;
    }

    private static async Task<Dictionary<long, int>> LoadReminderCountsAsync(
        SqliteConnection connection,
        long userId,
        CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = """
            select h.id, count(t.id)
            from habits h
            left join habit_reminder_times t on t.habit_id = h.id
            where h.user_id = $userId
              and h.active = 1
            group by h.id
            """;
        command.Parameters.AddWithValue("$userId", userId);

        var counts = new Dictionary<long, int>();
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            counts[reader.GetInt64(0)] = reader.GetInt32(1);
        }

        return counts;
    }

    private static async Task<Dictionary<long, HashSet<int>>> LoadFrequencyDaysAsync(
        SqliteConnection connection,
        long userId,
        CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = """
            select h.id, d.day_of_week
            from habits h
            join habit_frequency_days d on d.habit_id = h.id
            where h.user_id = $userId
              and h.active = 1
            """;
        command.Parameters.AddWithValue("$userId", userId);

        var days = new Dictionary<long, HashSet<int>>();
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            var habitId = reader.GetInt64(0);
            if (!days.TryGetValue(habitId, out var habitDays))
            {
                habitDays = [];
                days[habitId] = habitDays;
            }
            habitDays.Add(reader.GetInt32(1));
        }

        return days;
    }

    private static async Task<Dictionary<DateOnly, long>> LoadEntriesAsync(
        SqliteConnection connection,
        long userId,
        DateOnly startDate,
        DateOnly endDate,
        CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = """
            select id, entry_date
            from day_entries
            where user_id = $userId
              and entry_date between $startDate and $endDate
            """;
        command.Parameters.AddWithValue("$userId", userId);
        command.Parameters.AddWithValue("$startDate", ToDateText(startDate));
        command.Parameters.AddWithValue("$endDate", ToDateText(endDate));

        var entries = new Dictionary<DateOnly, long>();
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            entries[DateOnly.Parse(reader.GetString(1))] = reader.GetInt64(0);
        }

        return entries;
    }

    private static async Task<List<PlanRow>> LoadPlansAsync(
        SqliteConnection connection,
        long userId,
        DateOnly startDate,
        DateOnly endDate,
        CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = """
            select p.id, p.day_entry_id, p.habit_id, p.completed
            from day_entry_habits p
            join day_entries e on e.id = p.day_entry_id
            join habits h on h.id = p.habit_id
            where e.user_id = $userId
              and h.user_id = $userId
              and h.active = 1
              and e.entry_date between $startDate and $endDate
              and coalesce(p.override_action, 'SELECTED') <> 'DESELECTED'
            """;
        command.Parameters.AddWithValue("$userId", userId);
        command.Parameters.AddWithValue("$startDate", ToDateText(startDate));
        command.Parameters.AddWithValue("$endDate", ToDateText(endDate));

        var plans = new List<PlanRow>();
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            plans.Add(new PlanRow(
                reader.GetInt64(0),
                reader.GetInt64(1),
                reader.GetInt64(2),
                IsTruthy(reader.GetValue(3))
            ));
        }

        return plans;
    }

    private static async Task<Dictionary<long, int>> LoadCompletedTimesAsync(
        SqliteConnection connection,
        long userId,
        DateOnly startDate,
        DateOnly endDate,
        CancellationToken cancellationToken)
    {
        await using var command = connection.CreateCommand();
        command.CommandText = """
            select c.day_entry_habit_id, count(c.id)
            from day_entry_habit_time_completions c
            join day_entry_habits p on p.id = c.day_entry_habit_id
            join day_entries e on e.id = p.day_entry_id
            where e.user_id = $userId
              and e.entry_date between $startDate and $endDate
              and c.completed = 1
            group by c.day_entry_habit_id
            """;
        command.Parameters.AddWithValue("$userId", userId);
        command.Parameters.AddWithValue("$startDate", ToDateText(startDate));
        command.Parameters.AddWithValue("$endDate", ToDateText(endDate));

        var counts = new Dictionary<long, int>();
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            counts[reader.GetInt64(0)] = reader.GetInt32(1);
        }

        return counts;
    }

    private static DayTotals CalculateManualDay(
        long entryId,
        IReadOnlyDictionary<long, List<PlanRow>> plansByEntryId,
        IReadOnlyDictionary<long, HabitRow> habitsById,
        IReadOnlyDictionary<long, int> reminderCounts,
        IReadOnlyDictionary<long, int> completedTimes)
    {
        if (!plansByEntryId.TryGetValue(entryId, out var plans))
        {
            return new DayTotals(0, 0);
        }

        var plannedUnits = 0;
        var completedUnits = 0;

        foreach (var plan in plans)
        {
            if (!habitsById.ContainsKey(plan.HabitId))
            {
                continue;
            }

            var unitCount = UnitsForHabit(plan.HabitId, reminderCounts);
            plannedUnits += unitCount;
            completedUnits += unitCount > 1
                ? Math.Min(completedTimes.GetValueOrDefault(plan.Id), unitCount)
                : plan.Completed ? 1 : 0;
        }

        return new DayTotals(plannedUnits, completedUnits);
    }

    private static DayTotals CalculateAutomaticDay(
        DateOnly date,
        IReadOnlyList<HabitRow> habits,
        IReadOnlyDictionary<long, int> reminderCounts,
        IReadOnlyDictionary<long, HashSet<int>> frequencyDays)
    {
        var plannedUnits = habits
            .Where((habit) => HabitOccursOnDate(habit, date, frequencyDays))
            .Sum((habit) => UnitsForHabit(habit.Id, reminderCounts));

        return new DayTotals(plannedUnits, 0);
    }

    private static bool HabitOccursOnDate(
        HabitRow habit,
        DateOnly date,
        IReadOnlyDictionary<long, HashSet<int>> frequencyDays)
    {
        var frequencyType = habit.FrequencyType.Trim().ToUpperInvariant();
        var dayOfWeek = (int)date.DayOfWeek;
        var apiDayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;

        return frequencyType switch
        {
            "EVERY_DAY" or "DAILY" => true,
            "WEEKDAYS" => apiDayOfWeek is >= 1 and <= 5,
            "WEEKENDS" => apiDayOfWeek is 6 or 7,
            "CUSTOM" => frequencyDays.TryGetValue(habit.Id, out var days) && days.Contains(apiDayOfWeek),
            _ => false
        };
    }

    private static int UnitsForHabit(long habitId, IReadOnlyDictionary<long, int> reminderCounts)
    {
        return Math.Max(1, reminderCounts.GetValueOrDefault(habitId));
    }

    private static int CalculateCurrentStreak(IReadOnlyList<DailyStats> dailySeries)
    {
        var streak = 0;
        for (var index = dailySeries.Count - 1; index >= 0; index -= 1)
        {
            var day = dailySeries[index];
            if (day.PlannedUnits == 0)
            {
                continue;
            }

            if (day.CompletedUnits < day.PlannedUnits)
            {
                break;
            }

            streak += 1;
        }

        return streak;
    }

    private static int CalculateBestStreak(IReadOnlyList<DailyStats> dailySeries)
    {
        var best = 0;
        var current = 0;

        foreach (var day in dailySeries)
        {
            if (day.PlannedUnits == 0)
            {
                continue;
            }

            if (day.CompletedUnits >= day.PlannedUnits)
            {
                current += 1;
                best = Math.Max(best, current);
            }
            else
            {
                current = 0;
            }
        }

        return best;
    }

    private static decimal Rate(int completedUnits, int plannedUnits)
    {
        return plannedUnits == 0
            ? 0
            : Math.Round((decimal)completedUnits * 100 / plannedUnits, 1);
    }

    private static bool IsTruthy(object value)
    {
        return value switch
        {
            bool boolean => boolean,
            long number => number == 1,
            int number => number == 1,
            string text => text == "1" || text.Equals("true", StringComparison.OrdinalIgnoreCase),
            _ => false
        };
    }

    private static string ToDateText(DateOnly date)
    {
        return date.ToString("yyyy-MM-dd");
    }

    private sealed record HabitRow(long Id, string FrequencyType);
    private sealed record PlanRow(long Id, long EntryId, long HabitId, bool Completed);
    private sealed record DayTotals(int PlannedUnits, int CompletedUnits);
}
