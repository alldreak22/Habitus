namespace Habitus.Stats;

public sealed record EvolutionStatsResponse(
    StatsPeriod Period,
    EvolutionSummary Summary,
    IReadOnlyList<DailyStats> DailySeries
);

public sealed record StatsPeriod(
    DateOnly StartDate,
    DateOnly EndDate,
    int Days
);

public sealed record EvolutionSummary(
    int ActiveHabits,
    int PlannedUnits,
    int CompletedUnits,
    decimal CompletionRate,
    int CurrentStreak,
    int BestStreak
);

public sealed record DailyStats(
    DateOnly Date,
    int PlannedUnits,
    int CompletedUnits,
    decimal CompletionRate
);
