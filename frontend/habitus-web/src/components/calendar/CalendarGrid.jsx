import calendarLabels from '../../content/calendarLabels.json';
import { buildCalendarDays, formatDateKey, isSameDay } from '../../utils/date.js';
import Button from '../Button.jsx';
import IconButton from '../IconButton.jsx';
import SelectDropdown from '../SelectDropdown.jsx';

const { months, weekDays, yearRange } = calendarLabels;
const MAX_HABIT_TIME_ROWS = 2;
const MAX_TIMES_PER_ROW = 2;

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
            const timeMarkers = (activity.markers ?? []).filter((marker) => marker.time);
            const dotMarkers = groupDotMarkers(activity.markers ?? []);
            const timeRows = groupTimeMarkers(timeMarkers);
            const visibleTimeRows = timeRows.slice(0, MAX_HABIT_TIME_ROWS);
            const hiddenTimeRows = Math.max(0, timeRows.length - visibleTimeRows.length);
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
                <span className="calendar-day-top">
                  <span className="calendar-day-number">{day.date.getDate()}</span>
                  <span className="calendar-day-status">
                    {isToday && <span className="today-badge">Hoje</span>}
                    {activity.completed ? (
                      <span className="completion-icon material-symbols-outlined" aria-hidden="true">
                        check_circle
                      </span>
                    ) : null}
                  </span>
                </span>
                <span className="calendar-day-middle">
                  {visibleTimeRows.length ? (
                    <span className="day-time-markers" aria-hidden="true">
                      {visibleTimeRows.map((row) => {
                        const visibleTimes = row.times.slice(0, MAX_TIMES_PER_ROW);
                        const hiddenTimes = row.times.length - visibleTimes.length;

                        return (
                          <span key={row.id} className="day-time-marker">
                            <span className="day-time-dot" style={{ backgroundColor: row.color }} />
                            {visibleTimes.map((time) => (
                              <span key={time} className="day-time-value">
                                {time}
                              </span>
                            ))}
                            {hiddenTimes > 0 ? <span className="day-time-more">+{hiddenTimes}</span> : null}
                          </span>
                        );
                      })}
                      {hiddenTimeRows ? (
                        <span className="day-time-marker day-time-overflow">
                          +{hiddenTimeRows}
                        </span>
                      ) : null}
                    </span>
                  ) : null}
                </span>
                <span className="calendar-day-footer">
                  {dotMarkers.length ? (
                    <span className="day-markers" aria-hidden="true">
                      {dotMarkers.map((marker) => (
                        <span key={marker.id} className="day-marker" style={{ backgroundColor: marker.color }} />
                      ))}
                    </span>
                  ) : null}
                </span>
              </button>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function groupTimeMarkers(markers) {
  const rows = new Map();

  markers.forEach((marker) => {
    const rowKey = String(marker.habitId ?? marker.name ?? marker.id);
    const currentRow = rows.get(rowKey) ?? {
      color: marker.color,
      id: rowKey,
      name: marker.name,
      times: [],
    };

    currentRow.times.push(marker.time);
    rows.set(rowKey, currentRow);
  });

  return Array.from(rows.values()).map((row) => ({
    ...row,
    times: [...new Set(row.times)].sort(),
  }));
}

function groupDotMarkers(markers) {
  const rows = new Map();

  markers.forEach((marker) => {
    const rowKey = String(marker.habitId ?? marker.name ?? marker.id);
    if (!rows.has(rowKey)) {
      rows.set(rowKey, {
        color: marker.color,
        id: rowKey,
        name: marker.name,
      });
    }
  });

  return Array.from(rows.values());
}
