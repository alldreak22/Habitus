import habitsMock from '../content/habitsMock.json';

export async function getHabits() {
  return habitsMock.habits;
}

export async function getWeeklyHabitProgress() {
  return habitsMock.weeklyProgress;
}
