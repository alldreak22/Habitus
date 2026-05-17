import calendarLabels from '../../content/calendarLabels.json';
import { buildCalendarDays, formatDateKey, isSameDay } from '../../utils/date.js';
import Button from '../Button.jsx';
import IconButton from '../IconButton.jsx';
import SelectDropdown from '../SelectDropdown.jsx';

const { months, weekDays, yearRange } = calendarLabels;

export default function CalendarGrid({
  activityByDate,
  onEditDay,
  onMonthYearChange,
  onNextMonth,
  onPreviousMonth,
  onDayDoubleClick,
  onSelectDate,
  selectedDate,
  today,
  visibleMonth,
}) {
  const days = buildCalendarDays(visibleMonth);
  const yearOptions = Array.from(
    { length: yearRange.end - yearRange.start + 1 },
    (_, index) => yearRange.start + index,
  );

  function handleMonthChange(month) {
    onMonthYearChange(new Date(visibleMonth.getFullYear(), Number(month), 1));
  }

  function handleYearChange(year) {
    onMonthYearChange(new Date(Number(year), visibleMonth.getMonth(), 1));
  }

  return (
    <section className="calendar-card" aria-label="Calendário mensal">
      <div className="calendar-toolbar">
        <div className="calendar-month-controls">
          <div className="calendar-selectors">
            <label>
              <span>Mês</span>
              <SelectDropdown
                label="Mês"
                options={months.map((month, index) => ({ label: month, value: index }))}
                value={visibleMonth.getMonth()}
                onChange={handleMonthChange}
              />
            </label>
            <label>
              <span>Ano</span>
              <SelectDropdown
                label="Ano"
                options={yearOptions.map((year) => ({ label: year, value: year }))}
                value={visibleMonth.getFullYear()}
                onChange={handleYearChange}
              />
            </label>
          </div>
          <div className="icon-button-group">
            <IconButton icon="chevron_left" label="Mês anterior" onClick={onPreviousMonth} />
            <IconButton icon="chevron_right" label="Próximo mês" onClick={onNextMonth} />
          </div>
        </div>
        <Button variant="outline" onClick={onEditDay}>
          Editar Dia
        </Button>
      </div>
      <div className="calendar-body">
        <div className="calendar-weekdays">
          {weekDays.map((day) => (
            <span key={day}>{day}</span>
          ))}
        </div>
        <div className="calendar-days">
          {days.map((day) => {
            const dateKey = formatDateKey(day.date);
            const activity = activityByDate[dateKey] ?? {};
            const isToday = isSameDay(day.date, today);
            const isSelected = isSameDay(day.date, selectedDate);
            const className = [
              'calendar-day',
              day.isCurrentMonth ? '' : 'muted',
              isToday ? 'today' : '',
              isSelected ? 'selected' : '',
              activity.completed ? 'completed' : '',
            ]
              .filter(Boolean)
              .join(' ');

            return (
              <button
                key={dateKey}
                className={className}
                type="button"
                onDoubleClick={() => onDayDoubleClick?.(day.date)}
                onClick={() => onSelectDate(day.date)}
                aria-pressed={isSelected}
                aria-label={`Selecionar dia ${day.date.toLocaleDateString('pt-BR')}`}
              >
                <span className="calendar-day-number">{day.date.getDate()}</span>
                {isToday && <span className="today-badge">Hoje</span>}
                {activity.completed ? (
                  <span className="completion-icon material-symbols-outlined" aria-hidden="true">
                    check_circle
                  </span>
                ) : null}
                {activity.markers?.length ? (
                  <span className="day-markers" aria-hidden="true">
                    {activity.markers.map((marker) => (
                      <span
                        key={marker.id}
                        className="day-marker"
                        style={{ backgroundColor: marker.color }}
                      />
                    ))}
                  </span>
                ) : null}
              </button>
            );
          })}
        </div>
      </div>
    </section>
  );
}
