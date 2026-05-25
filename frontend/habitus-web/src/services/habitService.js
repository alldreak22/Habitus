import { apiRequest } from './api.js';

export async function getHabits() {
  const habits = await apiRequest('/habits');
  return habits.map(normalizeHabit);
}

export async function createHabit(habit) {
  const createdHabit = await apiRequest('/habits', {
    method: 'POST',
    body: JSON.stringify(habit),
  });
  return normalizeHabit(createdHabit);
}

export async function updateHabit(id, habit) {
  const updatedHabit = await apiRequest(`/habits/${id}`, {
    method: 'PUT',
    body: JSON.stringify(habit),
  });
  return normalizeHabit(updatedHabit);
}

export async function deleteHabit(id) {
  return apiRequest(`/habits/${id}`, {
    method: 'DELETE',
  });
}

function normalizeHabit(habit) {
  const reminderTimes = habit.reminderTimes ?? [];

  return {
    ...habit,
    active: habit.active ?? habit.status === 'ACTIVE',
    reminderEnabled: habit.reminder ?? false,
    suggestedTimes: reminderTimes.length ? reminderTimes.join(', ') : habit.suggestedTimes,
    targetFrequency: formatFrequency(habit.frequencyType ?? habit.targetFrequency),
  };
}

function formatFrequency(frequencyType) {
  if (frequencyType === 'EVERY_DAY' || frequencyType === 'DAILY') {
    return 'Todos os dias';
  }

  if (frequencyType === 'WEEKDAYS') {
    return 'Segunda a sexta';
  }

  if (frequencyType === 'WEEKENDS') {
    return 'Finais de semana';
  }

  if (frequencyType === 'CUSTOM') {
    return 'Personalizado';
  }

  return frequencyType;
}
