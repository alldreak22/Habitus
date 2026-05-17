import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Button from '../components/Button.jsx';
import IconButton from '../components/IconButton.jsx';
import TipCard from '../components/TipCard.jsx';
import DaySummary from '../components/calendar/DaySummary.jsx';
import HabitChoiceDropdown from '../components/habits/HabitChoiceDropdown.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import SegmentedSettingControl from '../components/settings/SegmentedSettingControl.jsx';
import ToggleSwitch from '../components/settings/ToggleSwitch.jsx';
import habitFormContent from '../content/habitFormContent.json';
import { getDaySummary } from '../services/calendarService.js';
import { getHabits, getWeeklyHabitProgress } from '../services/habitService.js';
import { formatDateKey, getToday } from '../utils/date.js';

const { colorOptions, iconOptions, initialForm, productivityTips, weekDays } = habitFormContent;
const frequencyOptions = [
  { label: 'Todos os dias', value: 'daily' },
  { label: 'Dias úteis', value: 'weekdays' },
  { label: 'Finais de Semana', value: 'weekends' },
  { label: 'Personalizado', value: 'custom' },
];
const statusOptions = [
  { label: 'Ativo', value: 'active' },
  { label: 'Inativo', value: 'inactive' },
];

export default function HabitsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const today = useMemo(() => getToday(), []);
  const [editingHabit, setEditingHabit] = useState(null);
  const [habitPendingDeletion, setHabitPendingDeletion] = useState(null);
  const [habits, setHabits] = useState([]);
  const [isCreating, setIsCreating] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [todaySummary, setTodaySummary] = useState([]);
  const [weeklyProgress, setWeeklyProgress] = useState(null);

  useEffect(() => {
    getHabits().then(setHabits);
    getWeeklyHabitProgress().then(setWeeklyProgress);
    getDaySummary(formatDateKey(today)).then(setTodaySummary);
  }, [today]);

  useEffect(() => {
    if (location.state?.startCreatingHabit) {
      setEditingHabit(null);
      setIsCreating(true);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

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

function HabitForm({ habit, onCancel, onSubmit }) {
  const [form, setForm] = useState(() => buildFormState(habit));
  const isCustomFrequency = form.frequency === 'custom';
  const selectedIcon = iconOptions.find((option) => option.icon === form.icon) ?? iconOptions[0];

  function updateForm(field, value) {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  }

  function updateSuggestedTime(index, value) {
    setForm((currentForm) => ({
      ...currentForm,
      suggestedTimes: currentForm.suggestedTimes.map((time, timeIndex) =>
        timeIndex === index ? value : time,
      ),
    }));
  }

  function addSuggestedTime() {
    setForm((currentForm) => ({
      ...currentForm,
      suggestedTimes: [...currentForm.suggestedTimes, '08:00'],
    }));
  }

  function removeSuggestedTime(index) {
    setForm((currentForm) => ({
      ...currentForm,
      suggestedTimes:
        currentForm.suggestedTimes.length > 1
          ? currentForm.suggestedTimes.filter((_, timeIndex) => timeIndex !== index)
          : [''],
    }));
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
      reminderEnabled: form.reminderEnabled,
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
          <h1 id="habits-heading">
            {habit ? 'Configure seu Hábito' : 'Configure seu novo Hábito'}
          </h1>
        </header>

        <form className="habit-form" onSubmit={handleSubmit}>
          <div className="habit-form-grid compact">
            <label className="form-field form-field-choice">
              <span>Ícone</span>
              <HabitChoiceDropdown
                label="Selecionar ícone do hábito"
                renderValue={() => (
                  <span className="material-symbols-outlined" aria-hidden="true">
                    {selectedIcon.icon}
                  </span>
                )}
              >
                {(closeDropdown) => (
                  <div className="habit-dropdown-icon-grid">
                    {iconOptions.map((option) => (
                      <button
                        key={option.icon}
                        className={form.icon === option.icon ? 'icon-choice selected' : 'icon-choice'}
                        type="button"
                        onClick={() => {
                          updateForm('icon', option.icon);
                          closeDropdown();
                        }}
                        aria-label={option.label}
                      >
                        <span className="material-symbols-outlined" aria-hidden="true">
                          {option.icon}
                        </span>
                      </button>
                    ))}
                  </div>
                )}
              </HabitChoiceDropdown>
            </label>

            <label className="form-field form-field-choice">
              <span>Cor</span>
              <HabitChoiceDropdown
                label="Selecionar cor do hábito"
                renderValue={() => <i className="habit-selected-color" style={{ backgroundColor: form.color }} />}
              >
                {(closeDropdown) => (
                  <div className="habit-dropdown-color-grid">
                    {colorOptions.map((color) => (
                      <button
                        key={color}
                        className={form.color === color ? 'color-choice selected' : 'color-choice'}
                        style={{ backgroundColor: color }}
                        type="button"
                        onClick={() => {
                          updateForm('color', color);
                          closeDropdown();
                        }}
                        aria-label={`Selecionar cor ${color}`}
                      />
                    ))}
                  </div>
                )}
              </HabitChoiceDropdown>
            </label>

            <label className="form-field title-field">
              <span>Título</span>
              <input
                type="text"
                value={form.name}
                onChange={(event) => updateForm('name', event.target.value)}
                placeholder="Ex: Exercício físico"
                required
              />
            </label>

            <label className="form-field description-field">
              <span>Descrição</span>
              <input
                type="text"
                value={form.description}
                onChange={(event) => updateForm('description', event.target.value)}
                placeholder="Ex: Treino funcional por 30 minutos"
              />
            </label>
          </div>

          <fieldset className="habit-control-row">
            <legend>Frequência</legend>
            <SegmentedSettingControl
              label="Frequência"
              options={frequencyOptions}
              value={form.frequency}
              onChange={(value) => updateForm('frequency', value)}
            />
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

          <div className="habit-control-row">
            <div>
              <span className="habit-control-label">Horários</span>
              <small>Defina os momentos ideais para este hábito.</small>
            </div>
            <div className="habit-time-list">
              {form.suggestedTimes.map((time, index) => (
                <div className="habit-time-chip" key={index}>
                  <input
                    type="time"
                    value={time}
                    onChange={(event) => updateSuggestedTime(index, event.target.value)}
                  />
                  <IconButton
                    icon="close"
                    label="Remover horário"
                    onClick={() => removeSuggestedTime(index)}
                  />
                </div>
              ))}
              <button className="habit-add-time" type="button" onClick={addSuggestedTime}>
                <span className="material-symbols-outlined" aria-hidden="true">
                  add
                </span>
                Adicionar horário
              </button>
            </div>
          </div>

          <div className="habit-control-row">
            <div>
              <span className="habit-control-label">Lembrete</span>
              <small>Receber notificação para não esquecer.</small>
            </div>
            <ToggleSwitch
              checked={form.reminderEnabled}
              label="Ativar lembrete do hábito"
              onChange={(value) => updateForm('reminderEnabled', value)}
            />
          </div>

          <div className="habit-control-row">
            <div>
              <span className="habit-control-label">Status</span>
              <small>Defina se o hábito começa ativo na rotina.</small>
            </div>
            <SegmentedSettingControl
              label="Status do hábito"
              options={statusOptions}
              value={form.active ? 'active' : 'inactive'}
              onChange={(value) => updateForm('active', value === 'active')}
            />
          </div>

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

  if (form.frequency === 'weekends') {
    return 'Finais de semana';
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
    frequency: habit.targetFrequency === 'Finais de semana' ? 'weekends' : 'daily',
    icon: habit.icon,
    name: habit.name,
    reminderEnabled: true,
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
