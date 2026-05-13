import { formatFullDate } from '../../utils/date.js';
import Button from '../Button.jsx';

export default function DaySummary({ date, habits, onToggleHabit }) {
  return (
    <section className="summary-card">
      <div className="summary-heading">
        <h4>Resumo do Dia</h4>
        <p>{formatFullDate(date)}</p>
      </div>
      <div className="summary-list" aria-live="polite">
        {habits.length ? (
          habits.map((habit) => (
            <HabitSummaryItem key={habit.id} habit={habit} onToggleHabit={onToggleHabit} />
          ))
        ) : (
          <p className="empty-state">Nenhum hábito planejado para este dia.</p>
        )}
      </div>
      <Button className="summary-details-button" fullWidth variant="outline">
        Ver detalhes do dia
      </Button>
    </section>
  );
}

function HabitSummaryItem({ habit, onToggleHabit }) {
  return (
    <article className={habit.completed ? 'habit-summary-item' : 'habit-summary-item pending'}>
      <div
        className={`habit-icon ${habit.tone}`}
        style={
          habit.color
            ? {
                backgroundColor: `${habit.color}1f`,
                color: habit.color,
              }
            : undefined
        }
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          {habit.icon}
        </span>
      </div>
      <div className="habit-summary-copy">
        <h5>{habit.name}</h5>
        <p>{habit.detail}</p>
      </div>
      <button
        className={`habit-status-button ${habit.completed ? 'done' : ''}`}
        type="button"
        onClick={() => onToggleHabit?.(habit.id)}
        aria-pressed={habit.completed}
        aria-label={
          habit.completed ? `Marcar ${habit.name} como pendente` : `Marcar ${habit.name} como concluído`
        }
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          {habit.completed ? 'check_circle' : 'radio_button_unchecked'}
        </span>
      </button>
    </article>
  );
}
