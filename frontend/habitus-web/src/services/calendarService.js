import calendarMock from '../content/calendarMock.json';
import habitsMock from '../content/habitsMock.json';
import productivityInsights from '../content/productivityInsights.json';

export async function getCalendarActivity() {
  const habitsById = new Map(habitsMock.habits.map((habit) => [habit.id, habit]));

  return Object.fromEntries(
    Object.entries(calendarMock.monthActivity).map(([dateKey, activity]) => {
      const plannedHabits = activity.plannedHabitIds
        .map((habitId) => habitsById.get(habitId))
        .filter(Boolean);
      const daySummary = calendarMock.daySummaries[dateKey] ?? [];
      const completedHabitIds = new Set(
        daySummary.filter((summary) => summary.completed).map((summary) => summary.id),
      );
      const completed =
        plannedHabits.length > 0 && plannedHabits.every((habit) => completedHabitIds.has(habit.id));

      return [
        dateKey,
        {
          ...activity,
          completed,
          markers: plannedHabits.map((habit) => ({
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
  const habitsById = new Map(habitsMock.habits.map((habit) => [habit.id, habit]));

  return (calendarMock.daySummaries[dateKey] ?? [])
    .map((summary) => {
      const habit = habitsById.get(summary.id);

      if (!habit) {
        return null;
      }

      return {
        ...summary,
        color: habit.color,
        detail: habit.description,
        icon: habit.icon,
        name: habit.name,
        tone: habit.tone,
      };
    })
    .filter(Boolean);
}

export async function getProductivityInsights() {
  return productivityInsights;
}
