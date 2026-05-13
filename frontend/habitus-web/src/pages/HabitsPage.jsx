import { useEffect, useMemo, useState } from 'react';
import Button from '../components/Button.jsx';
import IconButton from '../components/IconButton.jsx';
import TipCard from '../components/TipCard.jsx';
import DaySummary from '../components/calendar/DaySummary.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import habitFormContent from '../content/habitFormContent.json';
import { getDaySummary } from '../services/calendarService.js';
import { getHabits, getWeeklyHabitProgress } from '../services/habitService.js';
import { formatDateKey, getToday } from '../utils/date.js';

const { colorOptions, iconOptions, initialForm, productivityTips, weekDays } = habitFormContent;

export default function HabitsPage() {
  const today = useMemo(() => getToday(), []);
  const [editingHabit, setEditingHabit] = useState(null);
  const [habitPendingDeletion, setHabitPendingDeletion] = useState(null);
  const [habits, setHabits] = useState([]);
  const [isCreating, setIsCreating] = useState(false);
  const [lastCreatedCount, setLastCreatedCount] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [todaySummary, setTodaySummary] = useState([]);
  const [weeklyProgress, setWeeklyProgress] = useState(null);

  useEffect(() => {
    getHabits().then(setHabits);
    getWeeklyHabitProgress().then(setWeeklyProgress);
    getDaySummary(formatDateKey(today)).then(setTodaySummary);
  }, [today]);

  const activeCount = useMemo(() => habits.filter((habit) => habit.active).length, [habits]);
  const filteredHabits = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    if (!normalizedSearch) {
      return habits;
    }

    return habits.filter((habit) =>
      [habit.name, habit.description, habit.targetFrequency]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalizedSearch)),
    );
  }, [habits, searchTerm]);

  function handleCreateHabit(newHabit) {
    setHabits((currentHabits) => {
      if (editingHabit) {
        return currentHabits.map((habit) => (habit.id === editingHabit.id ? newHabit : habit));
      }

      return [newHabit, ...currentHabits];
    });
    setLastCreatedCount((count) => count + 1);
    setEditingHabit(null);
    setIsCreating(false);
    setSearchTerm('');
  }

  function handleEditHabit(habit) {
    setEditingHabit(habit);
    setIsCreating(true);
  }

  function handleCancelForm() {
    setEditingHabit(null);
    setIsCreating(false);
  }

  function handleToggleHabitStatus(habitId) {
    setHabits((currentHabits) =>
      currentHabits.map((habit) =>
        habit.id === habitId ? { ...habit, active: !habit.active } : habit,
      ),
    );
  }

  function handleConfirmDeleteHabit() {
    if (!habitPendingDeletion) {
      return;
    }

    setHabits((currentHabits) =>
      currentHabits.filter((habit) => habit.id !== habitPendingDeletion.id),
    );
    setTodaySummary((currentSummary) =>
      currentSummary.filter((habit) => habit.id !== habitPendingDeletion.id),
    );
    setHabitPendingDeletion(null);
  }

  function handleToggleTodaySummary(habitId) {
    setTodaySummary((currentSummary) =>
      currentSummary.map((habit) =>
        habit.id === habitId ? { ...habit, completed: !habit.completed } : habit,
      ),
    );
  }

  return (
    <>
      <TopBar
        title="Hábitos"
        searchValue={searchTerm}
        onSearchChange={isCreating ? undefined : setSearchTerm}
        searchPlaceholder="Filtrar hábitos"
      />
      <main className="content-area">
        <section className="habits-page" aria-labelledby="habits-heading">
          {isCreating ? (
            <HabitForm
              habit={editingHabit}
              onCancel={handleCancelForm}
              onSubmit={handleCreateHabit}
              tipRefreshKey={lastCreatedCount}
            />
          ) : (
            <div className="habits-dashboard">
              <section className="habits-main-column">
                <header className="habits-header">
                  <div>
                    <h1 id="habits-heading">Hábitos cadastrados</h1>
                    <p>Mantenha sua rotina visível, simples de revisar e fácil de ajustar.</p>
                  </div>
                  <Button icon="add" onClick={() => setIsCreating(true)}>
                    Novo hábito
                  </Button>
                </header>

                <div className="habits-scroll-region">
                  <div className="habit-metrics" aria-label="Resumo dos hábitos">
                    <article>
                      <span>{habits.length}</span>
                      <p>Cadastrados</p>
                    </article>
                    <article>
                      <span>{activeCount}</span>
                      <p>Ativos</p>
                    </article>
                    <article>
                      <span>{weeklyProgress?.completedPercentage ?? 0}%</span>
                      <p>Conclusão semanal</p>
                    </article>
                  </div>

                  <div className="habits-grid">
                    {filteredHabits.map((habit) => (
                      <HabitCard
                        key={habit.id}
                        habit={habit}
                        onDelete={setHabitPendingDeletion}
                        onEdit={handleEditHabit}
                        onToggleStatus={handleToggleHabitStatus}
                      />
                    ))}
                  </div>
                  {!filteredHabits.length && (
                    <p className="empty-state habits-empty-state">
                      Nenhum hábito encontrado para o filtro informado.
                    </p>
                  )}
                </div>
              </section>

              <HabitsSidePanel
                date={today}
                onToggleHabit={handleToggleTodaySummary}
                summary={todaySummary}
              />
            </div>
          )}
        </section>
      </main>
      {habitPendingDeletion ? (
        <ConfirmDialog
          message={`Tem certeza que deseja excluir "${habitPendingDeletion.name}"? Essa ação remove o hábito da listagem.`}
          onCancel={() => setHabitPendingDeletion(null)}
          onConfirm={handleConfirmDeleteHabit}
          title="Excluir hábito"
        />
      ) : null}
    </>
  );
}

function HabitForm({ habit, onCancel, onSubmit, tipRefreshKey }) {
  const [form, setForm] = useState(() => buildFormState(habit));
  const isCustomFrequency = form.frequency === 'custom';

  function updateForm(field, value) {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  }

  function toggleDay(dayKey) {
    setForm((currentForm) => {
      const selectedDays = currentForm.selectedDays.includes(dayKey)
        ? currentForm.selectedDays.filter((key) => key !== dayKey)
        : [...currentForm.selectedDays, dayKey];

      return { ...currentForm, selectedDays };
    });
  }

  function handleSubmit(event) {
    event.preventDefault();

    const name = form.name.trim();
    if (!name) {
      return;
    }

    onSubmit({
      active: form.active,
      color: form.color,
      description: form.description.trim(),
      icon: form.icon,
      id: habit?.id ?? `${Date.now()}`,
      name,
      suggestedTimes: form.suggestedTimes.filter(Boolean).join(', '),
      targetFrequency: buildFrequencyLabel(form),
      timesPerDay: form.timesPerDay,
    });

    setForm(initialForm);
  }

  return (
    <div className="habit-form-page">
      <button className="back-link" type="button" onClick={onCancel}>
        <span className="material-symbols-outlined" aria-hidden="true">
          arrow_back
        </span>
        Voltar para Hábitos
      </button>

      <div className="habit-form-card">
        <header className="habit-form-heading">
          <h1 id="habits-heading">{habit ? 'Editar Hábito' : 'Novo Hábito'}</h1>
          <p>Crie uma rotina com nome, cor, ícone e frequência claros.</p>
        </header>

        <form className="habit-form" onSubmit={handleSubmit}>
          <div className="habit-form-grid">
            <label className="form-field wide">
              <span>Nome</span>
              <input
                type="text"
                value={form.name}
                onChange={(event) => updateForm('name', event.target.value)}
                placeholder="Ex: Exercício físico"
                required
              />
            </label>

            <label className="form-field wide">
              <span>Descrição</span>
              <input
                type="text"
                value={form.description}
                onChange={(event) => updateForm('description', event.target.value)}
                placeholder="Ex: Treino funcional por 30 minutos"
              />
            </label>

            <label className="form-field status-field">
              <span>Status</span>
              <select
                value={form.active ? 'active' : 'inactive'}
                onChange={(event) => updateForm('active', event.target.value === 'active')}
              >
                <option value="active">Ativo</option>
                <option value="inactive">Inativo</option>
              </select>
            </label>
          </div>

          <fieldset className="choice-section">
            <legend>Ícone</legend>
            <div className="icon-choice-grid">
              {iconOptions.map((option) => (
                <button
                  key={option.icon}
                  className={form.icon === option.icon ? 'icon-choice selected' : 'icon-choice'}
                  type="button"
                  onClick={() => updateForm('icon', option.icon)}
                  aria-label={option.label}
                >
                  <span className="material-symbols-outlined" aria-hidden="true">
                    {option.icon}
                  </span>
                </button>
              ))}
            </div>
          </fieldset>

          <fieldset className="choice-section">
            <legend>Cor</legend>
            <div className="color-choice-grid">
              {colorOptions.map((color) => (
                <button
                  key={color}
                  className={form.color === color ? 'color-choice selected' : 'color-choice'}
                  style={{ backgroundColor: color }}
                  type="button"
                  onClick={() => updateForm('color', color)}
                  aria-label={`Selecionar cor ${color}`}
                />
              ))}
            </div>
          </fieldset>

          <fieldset className="frequency-section">
            <div className="frequency-heading">
              <legend>Frequência</legend>
              <p>Escolha quando este hábito entra na sua rotina.</p>
            </div>
            <div className="segmented-control" role="group" aria-label="Frequência">
              <button
                className={form.frequency === 'daily' ? 'selected' : ''}
                type="button"
                onClick={() => updateForm('frequency', 'daily')}
              >
                Todos os dias
              </button>
              <button
                className={form.frequency === 'weekdays' ? 'selected' : ''}
                type="button"
                onClick={() => updateForm('frequency', 'weekdays')}
              >
                Dias da semana
              </button>
              <button
                className={isCustomFrequency ? 'selected' : ''}
                type="button"
                onClick={() => updateForm('frequency', 'custom')}
              >
                Personalizado
              </button>
            </div>
            {isCustomFrequency && (
              <div className="custom-frequency-panel">
                <p>Dias selecionados</p>
                <div className="week-day-picker">
                  {weekDays.map((day) => (
                    <button
                      key={day.key}
                      className={form.selectedDays.includes(day.key) ? 'selected' : ''}
                      type="button"
                      onClick={() => toggleDay(day.key)}
                    >
                      {day.label}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </fieldset>

          <div className="execution-section">
            <label className="form-field compact">
              <span>Meta de execuções</span>
              <input
                min="1"
                type="number"
                value={form.timesPerDay}
                onChange={(event) => updateForm('timesPerDay', Number(event.target.value))}
              />
            </label>
            <label className="form-field compact">
              <span>Horário</span>
              <input
                type="time"
                value={form.suggestedTimes[0]}
                onChange={(event) => updateForm('suggestedTimes', [event.target.value])}
              />
            </label>
          </div>

          <div className="reminder-row">
            <div>
              <span className="material-symbols-outlined" aria-hidden="true">
                notifications
              </span>
              <div>
                <p>Lembrete diário</p>
                <small>Receber notificação para não esquecer.</small>
              </div>
            </div>
            <label className="switch">
              <input type="checkbox" defaultChecked />
              <span />
            </label>
          </div>

          <TipCard
            icon="auto_awesome"
            items={productivityTips}
            refreshKey={tipRefreshKey}
            title="Dica de Produtividade"
          />

          <footer className="habit-form-actions">
            <button className="secondary-action" type="button" onClick={onCancel}>
              Cancelar
            </button>
            <button className="primary-action" type="submit">
              {habit ? 'Salvar alterações' : 'Salvar Hábito'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
}

function buildFrequencyLabel(form) {
  if (form.frequency === 'daily') {
    return 'Todos os dias';
  }

  if (form.frequency === 'weekdays') {
    return 'Segunda a sexta';
  }

  return `${form.selectedDays.length || 0} dias personalizados`;
}

function buildFormState(habit) {
  if (!habit) {
    return initialForm;
  }

  return {
    active: habit.active,
    color: habit.color,
    description: habit.description,
    frequency: 'daily',
    icon: habit.icon,
    name: habit.name,
    selectedDays: initialForm.selectedDays,
    suggestedTimes: habit.suggestedTimes ? [habit.suggestedTimes.split(', ')[0]] : [''],
    timesPerDay: habit.timesPerDay,
  };
}

function HabitCard({ habit, onDelete, onEdit, onToggleStatus }) {
  return (
    <article className={habit.active ? 'habit-card' : 'habit-card inactive'}>
      <div className="habit-card-top">
        <div
          className="habit-card-icon"
          style={{
            backgroundColor: `${habit.color}1f`,
            color: habit.color,
          }}
        >
          <span className="material-symbols-outlined filled" aria-hidden="true">
            {habit.icon}
          </span>
        </div>
        <span className={habit.active ? 'habit-badge active' : 'habit-badge'}>
          {habit.active ? 'Ativo' : 'Inativo'}
        </span>
      </div>

      <div className="habit-card-copy">
        <h2>{habit.name}</h2>
        <p>{habit.description}</p>
      </div>

      <dl className="habit-details">
        <div>
          <dt>
            <span className="material-symbols-outlined" aria-hidden="true">
              sync
            </span>
            Frequência
          </dt>
          <dd>{habit.targetFrequency}</dd>
        </div>
        <div>
          <dt>
            <span className="material-symbols-outlined" aria-hidden="true">
              schedule
            </span>
            Horários
          </dt>
          <dd>{habit.suggestedTimes || 'Sem horário'}</dd>
        </div>
      </dl>

      <footer className="habit-card-actions">
        <div className="icon-button-group">
          <IconButton icon="edit" label={`Editar ${habit.name}`} onClick={() => onEdit(habit)} />
          <IconButton
            active={!habit.active}
            icon={habit.active ? 'visibility_off' : 'visibility'}
            label={habit.active ? `Desativar ${habit.name}` : `Reativar ${habit.name}`}
            onClick={() => onToggleStatus(habit.id)}
          />
        </div>
        <IconButton danger icon="delete" label={`Excluir ${habit.name}`} onClick={() => onDelete(habit)} />
      </footer>
    </article>
  );
}

function ConfirmDialog({ message, onCancel, onConfirm, title }) {
  return (
    <div className="dialog-backdrop" role="presentation">
      <section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title">
        <h2 id="dialog-title">{title}</h2>
        <p>{message}</p>
        <footer>
          <Button variant="secondary" onClick={onCancel}>
            Cancelar
          </Button>
          <Button onClick={onConfirm}>Excluir</Button>
        </footer>
      </section>
    </div>
  );
}

function HabitsSidePanel({ date, onToggleHabit, summary }) {
  return (
    <aside className="habits-analytics" aria-label="Análises dos hábitos">
      <DaySummary date={date} habits={summary} onToggleHabit={onToggleHabit} />
      <TipCard icon="auto_awesome" items={productivityTips} title="Dica de Produtividade" />
    </aside>
  );
}
