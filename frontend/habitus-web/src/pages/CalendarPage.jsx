import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import TipCard from '../components/TipCard.jsx';
import CalendarGrid from '../components/calendar/CalendarGrid.jsx';
import DayEditor from '../components/calendar/DayEditor.jsx';
import DaySummary from '../components/calendar/DaySummary.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import { useToast } from '../context/ToastContext.jsx';
import habitFormContent from '../content/habitFormContent.json';
import {
  getCalendarDay,
  getCalendarMonth,
  isHabitCompleted,
  mapCalendarActivity,
  mapCalendarDays,
  mapDayToActivity,
  saveDayEntry,
} from '../services/calendarService.js';
import { formatDateKey, getToday } from '../utils/date.js';

const { productivityTips } = habitFormContent;

export default function CalendarPage() {
  const { showToast } = useToast();
  const location = useLocation();
  const navigate = useNavigate();
  const today = useMemo(() => getToday(), []);
  const [visibleMonth, setVisibleMonth] = useState(
    () => new Date(today.getFullYear(), today.getMonth(), 1),
  );
  const [selectedDate, setSelectedDate] = useState(today);
  const [activityByDate, setActivityByDate] = useState({});
  const [daysByDate, setDaysByDate] = useState({});
  const [selectedDaySummary, setSelectedDaySummary] = useState([]);
  const [dayDescriptions, setDayDescriptions] = useState({});
  const [isEditingDay, setIsEditingDay] = useState(false);

  useEffect(() => {
    let isMounted = true;

    getCalendarMonth(visibleMonth)
      .then((monthData) => {
        if (!isMounted) {
          return;
        }
        setActivityByDate(mapCalendarActivity(monthData));
        setDaysByDate(mapCalendarDays(monthData));
      })
      .catch((error) => {
        if (isMounted) {
          showToast({ message: error.message || 'Nao foi possivel carregar o calendario.', type: 'warning' });
        }
      });

    return () => {
      isMounted = false;
    };
  }, [showToast, visibleMonth]);

  useEffect(() => {
    let isMounted = true;
    const selectedDateKey = formatDateKey(selectedDate);
    const selectedDay = daysByDate[selectedDateKey];

    async function loadSelectedDay() {
      try {
        const day = selectedDay ?? await getCalendarDay(selectedDateKey);
        if (!isMounted) {
          return;
        }
        setSelectedDaySummary(day?.habits ?? []);
        setDayDescriptions((currentDescriptions) => ({
          ...currentDescriptions,
          [selectedDateKey]: day?.description ?? currentDescriptions[selectedDateKey] ?? '',
        }));
      } catch (error) {
        if (isMounted) {
          showToast({ message: error.message || 'Nao foi possivel carregar o resumo do dia.', type: 'warning' });
        }
      }
    }

    loadSelectedDay();

    return () => {
      isMounted = false;
    };
  }, [daysByDate, selectedDate, showToast]);

  useEffect(() => {
    if (!location.state?.openDayEditor) {
      return;
    }

    const selectedDateFromState = location.state?.selectedDate;
    const parsedDate = selectedDateFromState ? new Date(`${selectedDateFromState}T00:00:00`) : today;
    const nextDate = Number.isNaN(parsedDate.getTime()) ? today : parsedDate;

    setVisibleMonth(new Date(nextDate.getFullYear(), nextDate.getMonth(), 1));
    handleOpenDayEditor(nextDate);
    navigate(location.pathname, { replace: true, state: null });
  }, [location.pathname, location.state, navigate, today]);

  function handlePreviousMonth() {
    setVisibleMonth(new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() - 1, 1));
  }

  function handleNextMonth() {
    setVisibleMonth(new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() + 1, 1));
  }

  function handleMonthYearChange(nextMonth) {
    setVisibleMonth(nextMonth);
  }

  function updateActivityFromSummary(date, nextSummary) {
    const selectedDateKey = formatDateKey(date);
    const allCompleted = nextSummary.length > 0 && nextSummary.every(isHabitCompleted);

    setActivityByDate((currentActivity) => ({
      ...currentActivity,
      [selectedDateKey]: {
        ...(currentActivity[selectedDateKey] ?? {}),
        completed: allCompleted,
        markers: buildActivityMarkers(nextSummary),
      },
    }));
  }

  function handleOpenDayEditor(date = selectedDate) {
    setSelectedDate(date);
    setIsEditingDay(true);
  }

  async function handleSaveDay({ description, habits }) {
    const selectedDateKey = formatDateKey(selectedDate);

    try {
      const savedDay = await saveDayEntry({
        dateKey: selectedDateKey,
        description,
        habits,
      });
      setDaysByDate((currentDays) => ({
        ...currentDays,
        [selectedDateKey]: savedDay,
      }));
      setDayDescriptions((currentDescriptions) => ({
        ...currentDescriptions,
        [selectedDateKey]: savedDay.description ?? description,
      }));
      setSelectedDaySummary(savedDay.habits ?? []);
      setActivityByDate((currentActivity) => ({
        ...currentActivity,
        [selectedDateKey]: mapDayToActivity(savedDay),
      }));
      setIsEditingDay(false);
      showToast({ message: 'Dia salvo com sucesso.', type: 'success' });
    } catch (error) {
      showToast({ message: error.message || 'Nao foi possivel salvar o dia.', type: 'warning' });
    }
  }

  async function handleToggleDaySummary(habitId, time = null) {
    const selectedDateKey = formatDateKey(selectedDate);
    const nextSummary = selectedDaySummary.map((habit) =>
      habit.id === habitId ? toggleHabitCompletion(habit, time) : habit,
    );

    setSelectedDaySummary(nextSummary);
    updateActivityFromSummary(selectedDate, nextSummary);

    try {
      const savedDay = await saveDayEntry({
        dateKey: selectedDateKey,
        description: dayDescriptions[selectedDateKey] ?? '',
        habits: nextSummary,
      });
      setDaysByDate((currentDays) => ({
        ...currentDays,
        [selectedDateKey]: savedDay,
      }));
      setSelectedDaySummary(savedDay.habits ?? []);
      setActivityByDate((currentActivity) => ({
        ...currentActivity,
        [selectedDateKey]: mapDayToActivity(savedDay),
      }));
    } catch (error) {
      showToast({ message: error.message || 'Nao foi possivel salvar o resumo do dia.', type: 'warning' });
      const day = daysByDate[selectedDateKey] ?? await getCalendarDay(selectedDateKey);
      setSelectedDaySummary(day?.habits ?? []);
      setActivityByDate((currentActivity) => ({
        ...currentActivity,
        [selectedDateKey]: mapDayToActivity(day),
      }));
    }
  }

  if (isEditingDay) {
    return (
      <>
        <TopBar />
        <main className="content-area">
          <DayEditor
            date={selectedDate}
            description={dayDescriptions[formatDateKey(selectedDate)]}
            habits={selectedDaySummary}
            onCancel={() => setIsEditingDay(false)}
            onSave={handleSaveDay}
          />
        </main>
      </>
    );
  }

  return (
    <>
      <TopBar />
      <main className="content-area">
        <section className="calendar-page" aria-labelledby="calendar-heading">
          <div className="calendar-dashboard">
            <section className="calendar-main-column">
              <header className="calendar-header">
                <div>
                  <h1 id="calendar-heading">Calendário</h1>
                  <p>Visualize sua rotina por mês e acompanhe os hábitos planejados para cada dia.</p>
                </div>
              </header>

              <CalendarGrid
                activityByDate={activityByDate}
                onEditDay={() => handleOpenDayEditor()}
                onMonthYearChange={handleMonthYearChange}
                onNextMonth={handleNextMonth}
                onPreviousMonth={handlePreviousMonth}
                onDayDoubleClick={handleOpenDayEditor}
                onSelectDate={setSelectedDate}
                selectedDate={selectedDate}
                today={today}
                visibleMonth={visibleMonth}
              />
            </section>
            <aside className="calendar-side-panel">
              <DaySummary
                date={selectedDate}
                habits={selectedDaySummary}
                onToggleHabit={handleToggleDaySummary}
                onViewDetails={() => handleOpenDayEditor()}
              />
              <TipCard icon="auto_awesome" items={productivityTips} title="Dica de Produtividade" />
            </aside>
          </div>
        </section>
      </main>
    </>
  );
}

function toggleHabitCompletion(habit, time = null) {
  if (!time) {
    return { ...habit, completed: !habit.completed };
  }

  const timeSlots = (habit.timeSlots ?? []).map((timeSlot) =>
    timeSlot.time === time ? { ...timeSlot, completed: !timeSlot.completed } : timeSlot,
  );

  return {
    ...habit,
    completed: timeSlots.length > 0 && timeSlots.every((timeSlot) => timeSlot.completed),
    timeSlots,
  };
}

function buildActivityMarkers(habits) {
  return habits.flatMap((habit) => {
    if (habit.timeSlots?.length) {
      return habit.timeSlots
        .map((timeSlot) => ({
          color: habit.color,
          completed: Boolean(timeSlot.completed),
          habitId: habit.id,
          id: `${habit.id}-${timeSlot.time}`,
          name: habit.name,
          time: timeSlot.time,
        }))
        .sort((first, second) => first.time.localeCompare(second.time));
    }

    return [{
      color: habit.color,
      completed: Boolean(habit.completed),
      habitId: habit.id,
      id: String(habit.id),
      name: habit.name,
      time: null,
    }];
  });
}
