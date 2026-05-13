import { useEffect, useMemo, useState } from 'react';
import CalendarGrid from '../components/calendar/CalendarGrid.jsx';
import DaySummary from '../components/calendar/DaySummary.jsx';
import ProductivityInsight from '../components/calendar/ProductivityInsight.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import {
  getCalendarActivity,
  getDaySummary,
  getProductivityInsights,
} from '../services/calendarService.js';
import { formatDateKey, getToday } from '../utils/date.js';

export default function CalendarPage() {
  const today = useMemo(() => getToday(), []);
  const [visibleMonth, setVisibleMonth] = useState(
    () => new Date(today.getFullYear(), today.getMonth(), 1),
  );
  const [selectedDate, setSelectedDate] = useState(today);
  const [activityByDate, setActivityByDate] = useState({});
  const [selectedDaySummary, setSelectedDaySummary] = useState([]);
  const [insights, setInsights] = useState([]);

  useEffect(() => {
    getCalendarActivity(visibleMonth).then(setActivityByDate);
  }, [visibleMonth]);

  useEffect(() => {
    getDaySummary(formatDateKey(selectedDate)).then(setSelectedDaySummary);
  }, [selectedDate]);

  useEffect(() => {
    getProductivityInsights().then(setInsights);
  }, []);

  function handlePreviousMonth() {
    setVisibleMonth((current) => new Date(current.getFullYear(), current.getMonth() - 1, 1));
  }

  function handleNextMonth() {
    setVisibleMonth((current) => new Date(current.getFullYear(), current.getMonth() + 1, 1));
  }

  function handleMonthYearChange(nextMonth) {
    setVisibleMonth(nextMonth);
  }

  function handleEditDay() {
    // Placeholder para a futura tela/fluxo de edição do dia.
  }

  function handleToggleDaySummary(habitId) {
    setSelectedDaySummary((currentSummary) => {
      const nextSummary = currentSummary.map((habit) =>
        habit.id === habitId ? { ...habit, completed: !habit.completed } : habit,
      );
      const selectedDateKey = formatDateKey(selectedDate);
      const allCompleted =
        nextSummary.length > 0 && nextSummary.every((habit) => habit.completed);

      setActivityByDate((currentActivity) => ({
        ...currentActivity,
        [selectedDateKey]: {
          ...(currentActivity[selectedDateKey] ?? {}),
          completed: allCompleted,
        },
      }));

      return nextSummary;
    });
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
                onEditDay={handleEditDay}
                onMonthYearChange={handleMonthYearChange}
                onNextMonth={handleNextMonth}
                onPreviousMonth={handlePreviousMonth}
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
              />
              <ProductivityInsight insights={insights} />
            </aside>
          </div>
        </section>
      </main>
    </>
  );
}
