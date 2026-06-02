import { apiRequest } from './api.js';

const monthCache = new Map();
const dayCache = new Map();
const inFlightRequests = new Map();
let cacheVersion = 0;

function monthPayload(referenceDate) {
  return {
    year: referenceDate.getFullYear(),
    month: referenceDate.getMonth() + 1,
  };
}

function monthKeyFromDate(referenceDate) {
  return [
    String(referenceDate.getFullYear()),
    String(referenceDate.getMonth() + 1).padStart(2, '0'),
  ].join('-');
}

function monthKeyFromDayKey(dateKey) {
  return dateKey.slice(0, 7);
}

function requestOnce(key, request) {
  if (inFlightRequests.has(key)) {
    return inFlightRequests.get(key);
  }

  let promise;
  promise = request().finally(() => {
    if (inFlightRequests.get(key) === promise) {
      inFlightRequests.delete(key);
    }
  });
  inFlightRequests.set(key, promise);
  return promise;
}

function rememberDay(day) {
  if (day?.date) {
    dayCache.set(day.date, day);
  }
  return day;
}

function rememberMonth(monthKey, monthData) {
  monthCache.set(monthKey, monthData);
  (monthData?.days ?? []).forEach(rememberDay);
  return monthData;
}

function replaceDayInMonth(day) {
  if (!day?.date) {
    return;
  }

  const monthKey = monthKeyFromDayKey(day.date);
  const monthData = monthCache.get(monthKey);
  if (!monthData) {
    return;
  }

  monthCache.set(monthKey, {
    ...monthData,
    days: monthData.days.map((currentDay) => (currentDay.date === day.date ? day : currentDay)),
  });
}

export function clearCalendarCache() {
  cacheVersion += 1;
  monthCache.clear();
  dayCache.clear();
  inFlightRequests.clear();
}

export async function getCalendarMonth(visibleMonth = new Date()) {
  const cacheKey = monthKeyFromDate(visibleMonth);
  const cachedMonth = monthCache.get(cacheKey);
  if (cachedMonth) {
    return cachedMonth;
  }

  const requestCacheVersion = cacheVersion;
  return requestOnce(`month:${cacheKey}`, async () => {
    const monthData = await apiRequest('/calendar/month', {
      method: 'POST',
      body: JSON.stringify(monthPayload(visibleMonth)),
    });
    if (requestCacheVersion !== cacheVersion) {
      return monthData;
    }
    return rememberMonth(cacheKey, monthData);
  });
}

export async function getCalendarDay(dateKey) {
  const cachedDay = dayCache.get(dateKey);
  if (cachedDay) {
    return cachedDay;
  }

  const requestCacheVersion = cacheVersion;
  return requestOnce(`day:${dateKey}`, async () => {
    const day = await apiRequest(`/calendar/days/${dateKey}`);
    if (requestCacheVersion !== cacheVersion) {
      return day;
    }
    return rememberDay(day);
  });
}

export function mapCalendarDays(monthData) {
  return Object.fromEntries((monthData?.days ?? []).map((day) => [day.date, day]));
}

export function mapCalendarActivity(monthData) {
  return Object.fromEntries(
    (monthData?.days ?? []).map((day) => [day.date, mapDayToActivity(day)]),
  );
}

export async function getCalendarActivity(visibleMonth = new Date()) {
  return mapCalendarActivity(await getCalendarMonth(visibleMonth));
}

export async function getDaySummary(dateKey) {
  const day = await getCalendarDay(dateKey);
  return day?.habits ?? [];
}

export async function saveDayEntry({ dateKey, description = '', habits = [] }) {
  const savedDay = await apiRequest(`/calendar/days/${dateKey}`, {
    method: 'PUT',
    body: JSON.stringify({
      description,
      habits: habits.map((habit) => ({
        habitId: habit.id,
        completed: Boolean(habit.completed),
      })),
    }),
  });

  rememberDay(savedDay);
  replaceDayInMonth(savedDay);
  return savedDay;
}

export function mapDayToActivity(day) {
  return {
    completed: Boolean(day?.completed),
    markers: (day?.markers ?? []).map((marker) => ({
      color: marker.color,
      id: marker.habitId,
      name: marker.name,
    })),
  };
}
