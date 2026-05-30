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
    targetFrequency: formatFrequency(habit.frequencyType ?? habit.targetFrequency, habit.frequencyDays),
  };
}

function formatFrequency(frequencyType, frequencyDays = []) {
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
    const selectedDays = formatFrequencyDays(frequencyDays);
    if (selectedDays) {
      return selectedDays;
    }
    return 'Personalizado';
  }

  return frequencyType;
}

function formatFrequencyDays(frequencyDays) {
  const dayLabels = {
    1: 'Seg',
    2: 'Ter',
    3: 'Qua',
    4: 'Qui',
    5: 'Sex',
    6: 'Sáb',
    7: 'Dom',
  };

  return [...new Set(frequencyDays)]
    .sort((firstDay, secondDay) => firstDay - secondDay)
    .map((day) => dayLabels[day])
    .filter(Boolean)
    .join(', ');
}
