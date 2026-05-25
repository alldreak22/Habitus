import { apiRequest } from './api.js';
import { getHabits } from './habitService.js';

function isNotFoundError(error) {
  return String(error?.message ?? '').includes('404');
}

function monthDateKeys(referenceDate) {
  const year = referenceDate.getFullYear();
  const month = referenceDate.getMonth();
  const lastDay = new Date(year, month + 1, 0).getDate();
  const keys = [];

  for (let day = 1; day <= lastDay; day += 1) {
    const date = new Date(year, month, day);
    const key = [
      String(date.getFullYear()),
      String(date.getMonth() + 1).padStart(2, '0'),
      String(date.getDate()).padStart(2, '0'),
    ].join('-');
    keys.push(key);
  }

  return keys;
}

export async function getCalendarActivity(visibleMonth = new Date()) {
  const dateKeys = monthDateKeys(visibleMonth);
  const summaryEntries = await Promise.all(
    dateKeys.map(async (dateKey) => {
      const summary = await getDaySummary(dateKey);
      return [dateKey, summary];
    }),
  );

  return Object.fromEntries(
    summaryEntries.map(([dateKey, summary]) => {
      const completed = summary.length > 0 && summary.every((habit) => habit.completed);
      return [
        dateKey,
        {
          completed,
          markers: summary.map((habit) => ({
            color: habit.color,
            id: habit.id,
            name: habit.name,
          })),
        },
      ];
    }),
  );
}

export async function getDaySummary(dateKey) {
  const habits = await getHabits();
  const habitsById = new Map(habits.map((habit) => [habit.id, habit]));
  let entry;
  try {
    entry = await apiRequest(`/daily-entries/date/${dateKey}`);
  } catch (error) {
    if (isNotFoundError(error)) {
      return [];
    }
    throw error;
  }

  const [plannedHabits, completedHabits] = await Promise.all([
    apiRequest(`/daily-entries/${entry.id}/planned-habits`),
    apiRequest(`/daily-entries/${entry.id}/completed-habits`),
  ]);
  const completedByHabitId = new Map(completedHabits.map((item) => [item.habitId, item]));

  return plannedHabits
    .map((planned) => {
      const habit = habitsById.get(planned.habitId);
      if (!habit) {
        return null;
      }
      const completion = completedByHabitId.get(planned.habitId);

      return {
        completed: Boolean(completion?.completed),
        color: habit.color,
        detail: habit.description,
        id: planned.habitId,
        icon: habit.icon,
        name: habit.name,
      };
    })
    .filter(Boolean);
}

export async function getProductivityInsights() {
  return [];
}
