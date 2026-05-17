import { useEffect, useMemo, useState } from 'react';
import CalendarGrid from '../components/calendar/CalendarGrid.jsx';
import DayEditor from '../components/calendar/DayEditor.jsx';
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
  const [dayDescriptions, setDayDescriptions] = useState({});
  const [isEditingDay, setIsEditingDay] = useState(false);
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

  function updateActivityFromSummary(date, nextSummary) {
    const selectedDateKey = formatDateKey(date);
    const allCompleted =
      nextSummary.length > 0 && nextSummary.every((habit) => habit.completed);

    setActivityByDate((currentActivity) => ({
      ...currentActivity,
      [selectedDateKey]: {
        ...(currentActivity[selectedDateKey] ?? {}),
        completed: allCompleted,
        markers: nextSummary.map((habit) => ({
          color: habit.color,
          id: habit.id,
          name: habit.name,
        })),
      },
    }));
  }

  function handleOpenDayEditor(date = selectedDate) {
    setSelectedDate(date);
    setIsEditingDay(true);
  }

  function handleSaveDay({ description, habits }) {
    const selectedDateKey = formatDateKey(selectedDate);

    setDayDescriptions((currentDescriptions) => ({
      ...currentDescriptions,
      [selectedDateKey]: description,
    }));
    setSelectedDaySummary(habits);
    updateActivityFromSummary(selectedDate, habits);
    setIsEditingDay(false);
  }

  function handleToggleDaySummary(habitId) {
    setSelectedDaySummary((currentSummary) => {
      const nextSummary = currentSummary.map((habit) =>
        habit.id === habitId ? { ...habit, completed: !habit.completed } : habit,
      );

      updateActivityFromSummary(selectedDate, nextSummary);
      return nextSummary;
    });
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
              <ProductivityInsight insights={insights} />
            </aside>
          </div>
        </section>
      </main>
    </>
  );
}
