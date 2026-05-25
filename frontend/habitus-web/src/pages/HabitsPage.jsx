import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Button from '../components/Button.jsx';
import IconButton from '../components/IconButton.jsx';
import TipCard from '../components/TipCard.jsx';
import DaySummary from '../components/calendar/DaySummary.jsx';
import HabitsEmptyState from '../components/habits/HabitsEmptyState.jsx';
import HabitChoiceDropdown from '../components/habits/HabitChoiceDropdown.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import SegmentedSettingControl from '../components/settings/SegmentedSettingControl.jsx';
import ToggleSwitch from '../components/settings/ToggleSwitch.jsx';
import habitFormContent from '../content/habitFormContent.json';
import { useToast } from '../context/ToastContext.jsx';
import { getDaySummary } from '../services/calendarService.js';
import { createHabit, deleteHabit, getHabits, updateHabit } from '../services/habitService.js';
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
const HABIT_TITLE_MAX_LENGTH = 255;
const HABIT_DESCRIPTION_MAX_LENGTH = 1000;

export default function HabitsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const today = useMemo(() => getToday(), []);
  const [editingHabit, setEditingHabit] = useState(null);
  const [habitPendingDeletion, setHabitPendingDeletion] = useState(null);
  const [habits, setHabits] = useState([]);
  const [isLoadingHabits, setIsLoadingHabits] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [todaySummary, setTodaySummary] = useState([]);

  useEffect(() => {
    let isMounted = true;

    async function loadHabits() {
      setIsLoadingHabits(true);

      try {
        const loadedHabits = await getHabits();
        if (isMounted) {
          setHabits(loadedHabits);
        }
      } catch (error) {
        if (isMounted) {
          showToast({ message: error.message || 'Nao foi possivel carregar os habitos da API.', type: 'warning' });
        }
      } finally {
        if (isMounted) {
          setIsLoadingHabits(false);
        }
      }
    }

    loadHabits();
    getDaySummary(formatDateKey(today)).then(setTodaySummary);

    return () => {
      isMounted = false;
    };
  }, [showToast, today]);

  useEffect(() => {
    if (location.state?.startCreatingHabit) {
      setEditingHabit(null);
      setIsCreating(true);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.pathname, location.state, navigate]);

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

  async function handleCreateHabit(form) {
    try {
      const payload = buildHabitPayload(form);
      const savedHabit = editingHabit
        ? await updateHabit(editingHabit.id, payload)
        : await createHabit(payload);

      setHabits((currentHabits) => {
        if (editingHabit) {
          return currentHabits.map((habit) => (habit.id === editingHabit.id ? savedHabit : habit));
        }

        return [savedHabit, ...currentHabits];
      });
      setEditingHabit(null);
      setIsCreating(false);
      setSearchTerm('');
      showToast({
        message: editingHabit ? 'Habito atualizado com sucesso.' : 'Habito criado com sucesso.',
        type: 'success',
      });
      return null;
    } catch (error) {
      const errorMessage = `Nao foi possivel salvar o habito. ${error.message}`;
      showToast({ message: errorMessage, type: 'warning' });
      return errorMessage;
    }
  }

  function handleEditHabit(habit) {
    setEditingHabit(habit);
    setIsCreating(true);
  }

  function handleCancelForm() {
    setEditingHabit(null);
    setIsCreating(false);
  }

  async function handleToggleHabitStatus(habitId) {
    const habit = habits.find((currentHabit) => currentHabit.id === habitId);
    if (!habit) {
      return;
    }

    try {
      const updatedHabit = await updateHabit(habitId, buildHabitPayload({ ...habit, active: !habit.active }));
      setHabits((currentHabits) =>
        currentHabits.map((currentHabit) =>
          currentHabit.id === habitId ? updatedHabit : currentHabit,
        ),
      );
      showToast({ message: 'Status do habito atualizado com sucesso.', type: 'success' });
    } catch (error) {
      showToast({ message: `Nao foi possivel atualizar o status do habito. ${error.message}`, type: 'warning' });
    }
  }

  async function handleConfirmDeleteHabit() {
    if (!habitPendingDeletion) {
      return;
    }

    try {
      await deleteHabit(habitPendingDeletion.id);
      setHabits((currentHabits) =>
        currentHabits.filter((habit) => habit.id !== habitPendingDeletion.id),
      );
      setTodaySummary((currentSummary) =>
        currentSummary.filter((habit) => habit.id !== habitPendingDeletion.id),
      );
      setHabitPendingDeletion(null);
      showToast({ message: 'Habito excluido com sucesso.', type: 'success' });
    } catch (error) {
      showToast({ message: `Nao foi possivel excluir o habito. ${error.message}`, type: 'warning' });
    }
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
              onValidationError={(message) => showToast({ message, type: 'warning' })}
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
                  {habits.length ? (
                    <Button icon="add" onClick={() => setIsCreating(true)}>
                      Novo hábito
                    </Button>
                  ) : null}
                </header>

                <div className="habits-scroll-region">
                  {isLoadingHabits ? (
                    <p className="empty-state habits-empty-state">Carregando habitos da API...</p>
                  ) : null}

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
                  {!isLoadingHabits && !filteredHabits.length ? (
                    <HabitsEmptyState onCreate={() => setIsCreating(true)} />
                  ) : null}
                </div>
              </section>

              <HabitsSidePanel
                date={today}
                onToggleHabit={handleToggleTodaySummary}
                onViewDetails={() =>
                  navigate('/calendario', {
                    state: { openDayEditor: true, selectedDate: formatDateKey(today) },
                  })
                }
                summary={todaySummary}
              />
            </div>
          )}
        </section>
      </main>
      {habitPendingDeletion ? (
        <ConfirmDialog
          message={`Tem certeza que deseja excluir "${habitPendingDeletion.name}"? Essa ação remove o hábito da listagem e do banco.`}
          onCancel={() => setHabitPendingDeletion(null)}
          onConfirm={handleConfirmDeleteHabit}
          title="Excluir hábito"
        />
      ) : null}
    </>
  );
}

function HabitForm({ habit, onCancel, onSubmit, onValidationError }) {
  const [form, setForm] = useState(() => buildFormState(habit));
  const [isSubmitting, setIsSubmitting] = useState(false);
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
    if (form.suggestedTimes.some((time) => !String(time ?? '').trim())) {
      onValidationError?.('Preencha ou remova o horario vazio antes de adicionar outro.');
      return;
    }

    setForm((currentForm) => ({
      ...currentForm,
      suggestedTimes: [...currentForm.suggestedTimes, ''],
    }));
  }

  function removeSuggestedTime(index) {
    setForm((currentForm) => ({
      ...currentForm,
      suggestedTimes: currentForm.suggestedTimes.filter((_, timeIndex) => timeIndex !== index),
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

  async function handleSubmit(event) {
    event.preventDefault();

    const name = form.name.trim();
    const description = form.description.trim();

    if (!name) {
      onValidationError?.('Informe um titulo para o habito.');
      return;
    }
    if (name.length > HABIT_TITLE_MAX_LENGTH) {
      onValidationError?.(`Titulo deve ter no maximo ${HABIT_TITLE_MAX_LENGTH} caracteres.`);
      return;
    }
    if (description.length > HABIT_DESCRIPTION_MAX_LENGTH) {
      onValidationError?.(`Descricao deve ter no maximo ${HABIT_DESCRIPTION_MAX_LENGTH} caracteres.`);
      return;
    }

    if (form.frequency === 'custom' && !form.selectedDays.length) {
      onValidationError?.('Selecione ao menos um dia para frequencia personalizada.');
      return;
    }

    setIsSubmitting(true);

    try {
      const errorMessage = await onSubmit({
        ...form,
        description,
        suggestedTimes: form.suggestedTimes.filter(Boolean),
        targetFrequency: buildFrequencyLabel(form),
        timesPerDay: form.suggestedTimes.filter(Boolean).length || form.timesPerDay,
        name,
      });
      if (errorMessage) {
        return;
      }
      setForm(initialForm);
    } finally {
      setIsSubmitting(false);
    }
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
                maxLength={HABIT_TITLE_MAX_LENGTH}
                required
              />
              <small className="field-counter">
                {form.name.length}/{HABIT_TITLE_MAX_LENGTH}
              </small>
            </label>

            <label className="form-field description-field">
              <span>Descrição</span>
              <input
                type="text"
                value={form.description}
                onChange={(event) => updateForm('description', event.target.value)}
                placeholder="Ex: Treino funcional por 30 minutos"
                maxLength={HABIT_DESCRIPTION_MAX_LENGTH}
              />
              <small className="field-counter">
                {form.description.length}/{HABIT_DESCRIPTION_MAX_LENGTH}
              </small>
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
            <button className="primary-action" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Salvando...' : habit ? 'Salvar alterações' : 'Salvar Hábito'}
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

function buildHabitPayload(habit) {
  const reminderTimes = Array.isArray(habit.suggestedTimes)
    ? habit.suggestedTimes.filter(Boolean)
    : String(habit.suggestedTimes ?? '')
        .split(',')
        .map((time) => time.trim())
        .filter(Boolean);
  const frequencyType = mapFrequencyToApi(habit.frequency ?? habit.frequencyType ?? habit.targetFrequency);
  const frequencyDays = frequencyType === 'CUSTOM'
    ? (habit.selectedDays ?? []).map(mapDayToApi).filter(Boolean)
    : [];

  return {
    name: habit.name,
    title: habit.name,
    icon: habit.icon,
    color: habit.color,
    description: habit.description,
    targetFrequency: frequencyType,
    timesPerDay: reminderTimes.length || habit.timesPerDay || 1,
    suggestedTimes: reminderTimes.join(','),
    reminder: Boolean(habit.reminderEnabled ?? habit.reminder),
    frequencyType,
    status: habit.active ? 'ACTIVE' : 'INACTIVE',
    reminderTimes,
    frequencyDays,
  };
}

function mapFrequencyToApi(frequency) {
  if (frequency === 'daily' || frequency === 'Todos os dias' || frequency === 'DAILY') {
    return 'EVERY_DAY';
  }

  if (frequency === 'weekdays' || frequency === 'Segunda a sexta') {
    return 'WEEKDAYS';
  }

  if (frequency === 'weekends' || frequency === 'Finais de semana') {
    return 'WEEKENDS';
  }

  return 'CUSTOM';
}

function mapDayToApi(day) {
  return {
    mon: 1,
    tue: 2,
    wed: 3,
    thu: 4,
    fri: 5,
    sat: 6,
    sun: 7,
  }[day];
}

function mapFrequencyToForm(habit) {
  const normalizedFrequencyType = String(habit.frequencyType ?? '').trim().toUpperCase();
  const normalizedTargetFrequency = String(habit.targetFrequency ?? '').trim().toUpperCase();

  if (normalizedFrequencyType === 'WEEKDAYS' || normalizedTargetFrequency === 'SEGUNDA A SEXTA') {
    return 'weekdays';
  }

  if (normalizedFrequencyType === 'WEEKENDS' || normalizedTargetFrequency === 'FINAIS DE SEMANA') {
    return 'weekends';
  }

  if (
    normalizedFrequencyType === 'CUSTOM'
    || normalizedTargetFrequency === 'CUSTOM'
    || normalizedTargetFrequency === 'PERSONALIZADO'
  ) {
    return 'custom';
  }

  if (
    normalizedFrequencyType === 'EVERY_DAY'
    || normalizedFrequencyType === 'DAILY'
    || normalizedTargetFrequency === 'DAILY'
    || normalizedTargetFrequency === 'TODOS OS DIAS'
  ) {
    return 'daily';
  }

  return 'daily';
}

function mapDayToForm(day) {
  return {
    1: 'mon',
    2: 'tue',
    3: 'wed',
    4: 'thu',
    5: 'fri',
    6: 'sat',
    7: 'sun',
  }[day];
}

function buildFormState(habit) {
  if (!habit) {
    return initialForm;
  }

  return {
    active: habit.active,
    color: habit.color,
    description: habit.description,
    frequency: mapFrequencyToForm(habit),
    icon: habit.icon,
    name: habit.name,
    reminderEnabled: habit.reminderEnabled ?? habit.reminder ?? true,
    selectedDays: habit.frequencyDays?.map(mapDayToForm).filter(Boolean) ?? initialForm.selectedDays,
    suggestedTimes: habit.suggestedTimes
      ? habit.suggestedTimes.split(',').map((time) => time.trim()).filter(Boolean)
      : [],
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

function HabitsSidePanel({ date, onToggleHabit, onViewDetails, summary }) {
  return (
    <aside className="habits-analytics" aria-label="Análises dos hábitos">
      <DaySummary
        date={date}
        habits={summary}
        onToggleHabit={onToggleHabit}
        onViewDetails={onViewDetails}
      />
      <TipCard icon="auto_awesome" items={productivityTips} title="Dica de Produtividade" />
    </aside>
  );
}
