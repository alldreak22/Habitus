import { useMemo, useState } from 'react';
import Button from '../Button.jsx';
import IconButton from '../IconButton.jsx';

export default function AddHabitToDayModal({
  existingHabitIds,
  habits,
  onClose,
  onConfirm,
  onCreateHabit,
}) {
  const [query, setQuery] = useState('');
  const [selectedIds, setSelectedIds] = useState([]);
  const existingIds = useMemo(() => new Set(existingHabitIds), [existingHabitIds]);
  const selectedIdSet = useMemo(() => new Set(selectedIds), [selectedIds]);
  const availableHabits = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return habits
      .filter((habit) => !existingIds.has(habit.id))
      .filter((habit) => {
        if (!normalizedQuery) return true;

        return [habit.name, habit.description, habit.targetFrequency]
          .filter(Boolean)
          .some((value) => value.toLowerCase().includes(normalizedQuery));
      });
  }, [existingIds, habits, query]);

  function toggleHabit(habitId) {
    setSelectedIds((currentIds) =>
      currentIds.includes(habitId)
        ? currentIds.filter((id) => id !== habitId)
        : [...currentIds, habitId],
    );
  }

  function handleConfirm() {
    onConfirm(habits.filter((habit) => selectedIdSet.has(habit.id)));
  }

  return (
    <div className="dialog-backdrop day-habit-modal-backdrop" role="presentation">
      <section className="day-habit-modal" role="dialog" aria-modal="true" aria-labelledby="add-habit-title">
        <header>
          <h2 id="add-habit-title">Adicionar Hábitos ao Dia</h2>
          <IconButton icon="close" label="Fechar modal" onClick={onClose} />
        </header>

        <label className="day-habit-search">
          <span className="material-symbols-outlined" aria-hidden="true">
            search
          </span>
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Buscar hábitos cadastrados"
          />
        </label>

        <div className="day-habit-list">
          {availableHabits.map((habit) => {
            const isSelected = selectedIdSet.has(habit.id);

            return (
              <button
                key={habit.id}
                className={isSelected ? 'day-habit-option selected' : 'day-habit-option'}
                type="button"
                aria-pressed={isSelected}
                onClick={() => toggleHabit(habit.id)}
              >
                <span
                  className="habit-icon"
                  style={{ backgroundColor: `${habit.color}1f`, color: habit.color }}
                  aria-hidden="true"
                >
                  <span className="material-symbols-outlined">{habit.icon}</span>
                </span>
                <span>{habit.name}</span>
                <span className="material-symbols-outlined" aria-hidden="true">
                  {isSelected ? 'check_circle' : 'add_circle'}
                </span>
              </button>
            );
          })}
          {!availableHabits.length ? (
            <p className="empty-state">Nenhum hábito disponível para adicionar.</p>
          ) : null}
        </div>

        <footer>
          <button className="create-habit-link" type="button" onClick={onCreateHabit}>
            <span className="material-symbols-outlined" aria-hidden="true">
              add
            </span>
            Criar novo hábito
          </button>
          <div>
            <Button variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button onClick={handleConfirm}>Confirmar</Button>
          </div>
        </footer>
      </section>
    </div>
  );
}
