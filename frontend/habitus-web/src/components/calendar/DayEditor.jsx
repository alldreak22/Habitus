import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getHabits } from '../../services/habitService.js';
import { formatFullDate } from '../../utils/date.js';
import Button from '../Button.jsx';
import IconButton from '../IconButton.jsx';
import AddHabitToDayModal from './AddHabitToDayModal.jsx';

const editorActions = [
  { command: 'bold', icon: 'format_bold', label: 'Negrito' },
  { command: 'italic', icon: 'format_italic', label: 'Itálico' },
  { command: 'insertUnorderedList', icon: 'format_list_bulleted', label: 'Lista com marcadores' },
  { command: 'insertOrderedList', icon: 'format_list_numbered', label: 'Lista numerada' },
  { command: 'formatBlock', icon: 'format_quote', label: 'Citação', value: 'blockquote' },
  { command: 'undo', icon: 'undo', label: 'Desfazer' },
  { command: 'redo', icon: 'redo', label: 'Refazer' },
];

export default function DayEditor({
  date,
  description,
  habits,
  onCancel,
  onSave,
}) {
  const navigate = useNavigate();
  const editorRef = useRef(null);
  const imageInputRef = useRef(null);
  const [allHabits, setAllHabits] = useState([]);
  const [editorHtml, setEditorHtml] = useState(description || '');
  const [isAddHabitOpen, setIsAddHabitOpen] = useState(false);
  const [plannedHabits, setPlannedHabits] = useState(habits);

  useEffect(() => {
    getHabits().then(setAllHabits);
  }, []);

  useEffect(() => {
    setPlannedHabits(habits);
  }, [habits]);

  useEffect(() => {
    const nextDescription = description || '';
    setEditorHtml(nextDescription);
    if (editorRef.current) {
      editorRef.current.innerHTML = nextDescription;
    }
  }, [description]);

  function focusEditor() {
    editorRef.current?.focus();
  }

  function runEditorCommand(command, value = null) {
    focusEditor();
    document.execCommand(command, false, value);
    setEditorHtml(editorRef.current?.innerHTML ?? '');
  }

  function insertLink() {
    const url = window.prompt('Informe o link');
    if (!url) return;
    runEditorCommand('createLink', url);
  }

  function handleImageSelect(event) {
    const [file] = event.target.files;
    if (!file) return;

    const imageUrl = URL.createObjectURL(file);
    runEditorCommand('insertImage', imageUrl);
    event.target.value = '';
  }

  function togglePlannedHabit(habitId, time = null) {
    setPlannedHabits((currentHabits) =>
      currentHabits.map((habit) =>
        habit.id === habitId ? toggleHabitCompletion(habit, time) : habit,
      ),
    );
  }

  function removePlannedHabit(habitId) {
    setPlannedHabits((currentHabits) => currentHabits.filter((habit) => habit.id !== habitId));
  }

  function handleAddHabits(nextHabits) {
    const existingIds = new Set(plannedHabits.map((habit) => habit.id));
    const habitsToAdd = nextHabits
      .filter((habit) => !existingIds.has(habit.id))
      .map((habit) => ({
        ...habit,
        completed: false,
        detail: habit.description,
        timeSlots: buildTimeSlots(habit),
      }));

    setPlannedHabits((currentHabits) => [...currentHabits, ...habitsToAdd]);
    setIsAddHabitOpen(false);
  }

  function handleCreateHabit() {
    navigate('/habitos', { state: { startCreatingHabit: true } });
  }

  function handleSave() {
    onSave({
      description: editorHtml,
      habits: plannedHabits,
    });
  }

  return (
    <>
      <section className="day-editor-page" aria-labelledby="day-editor-heading">
        <button className="back-link" type="button" onClick={onCancel}>
          <span className="material-symbols-outlined" aria-hidden="true">
            arrow_back
          </span>
          Voltar para Calendário
        </button>

        <header className="day-editor-header">
          <div>
            <h1 id="day-editor-heading">{formatFullDate(date)}</h1>
            <p>Registre o descritivo do dia e revise os hábitos planejados.</p>
          </div>
        </header>

        <div className="day-editor-layout">
          <section className="day-editor-card" aria-labelledby="day-description-heading">
            <h2 id="day-description-heading">Descritivo de atividades</h2>
            <div className="rich-editor">
              <div className="rich-editor-toolbar" aria-label="Ferramentas de texto">
                {editorActions.map((action) => (
                  <IconButton
                    key={`${action.command}-${action.icon}`}
                    icon={action.icon}
                    label={action.label}
                    onClick={() => runEditorCommand(action.command, action.value)}
                  />
                ))}
                <span className="toolbar-separator" aria-hidden="true" />
                <IconButton icon="link" label="Inserir link" onClick={insertLink} />
                <IconButton icon="image" label="Inserir imagem" onClick={() => imageInputRef.current?.click()} />
                <input
                  ref={imageInputRef}
                  className="profile-photo-input"
                  type="file"
                  accept="image/*"
                  onChange={handleImageSelect}
                />
              </div>
              <div
                ref={editorRef}
                className="rich-editor-content"
                contentEditable
                data-placeholder="Descreva seu dia, reflexões e progresso..."
                suppressContentEditableWarning
                onInput={(event) => setEditorHtml(event.currentTarget.innerHTML)}
              />
            </div>
            <footer>
              <Button icon="save" onClick={handleSave}>
                Salvar dia
              </Button>
              <Button variant="secondary" onClick={onCancel}>
                Cancelar
              </Button>
            </footer>
          </section>

          <aside className="day-habits-card" aria-labelledby="day-habits-heading">
            <header>
              <h2 id="day-habits-heading">Hábitos do dia</h2>
              <IconButton icon="add" label="Adicionar hábito ao dia" onClick={() => setIsAddHabitOpen(true)} />
            </header>
            <div className="day-habits-list">
              {plannedHabits.map((habit) => {
                const hasTimeSlots = Boolean(habit.timeSlots?.length);

                return (
                  <div
                    key={habit.id}
                    className={habit.completed ? 'day-habit-item completed' : 'day-habit-item'}
                  >
                    <div className="day-habit-main">
                      <button
                        className="day-habit-toggle"
                        type="button"
                        onClick={() => !hasTimeSlots && togglePlannedHabit(habit.id)}
                        aria-pressed={habit.completed}
                      >
                        <span
                          className="habit-icon"
                          style={{ backgroundColor: `${habit.color}1f`, color: habit.color }}
                          aria-hidden="true"
                        >
                          <span className="material-symbols-outlined">
                            {habit.completed ? 'check' : habit.icon}
                          </span>
                        </span>
                        <span className="day-habit-name">{habit.name}</span>
                      </button>
                      {hasTimeSlots ? (
                        <div className="habit-time-toggle-list" aria-label={`Horários de ${habit.name}`}>
                          {habit.timeSlots.map((timeSlot) => (
                            <button
                              key={timeSlot.time}
                              className={timeSlot.completed ? 'habit-time-toggle done' : 'habit-time-toggle'}
                              type="button"
                              onClick={() => togglePlannedHabit(habit.id, timeSlot.time)}
                              aria-pressed={timeSlot.completed}
                            >
                              <span
                                className="habit-time-dot"
                                style={{ backgroundColor: habit.color }}
                                aria-hidden="true"
                              />
                              {timeSlot.time}
                            </button>
                          ))}
                        </div>
                      ) : null}
                    </div>
                    <IconButton
                      icon="close"
                      label={`Remover ${habit.name} deste dia`}
                      onClick={() => removePlannedHabit(habit.id)}
                    />
                  </div>
                );
              })}
            </div>
            <button className="add-day-habit-button" type="button" onClick={() => setIsAddHabitOpen(true)}>
              <span className="material-symbols-outlined" aria-hidden="true">
                add_circle
              </span>
              Adicionar novo hábito
            </button>
          </aside>
        </div>
      </section>

      {isAddHabitOpen ? (
        <AddHabitToDayModal
          existingHabitIds={plannedHabits.map((habit) => habit.id)}
          habits={allHabits}
          onClose={() => setIsAddHabitOpen(false)}
          onConfirm={handleAddHabits}
          onCreateHabit={handleCreateHabit}
        />
      ) : null}
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

function buildTimeSlots(habit) {
  const times = Array.isArray(habit.reminderTimes)
    ? habit.reminderTimes
    : String(habit.suggestedTimes ?? '')
        .split(',')
        .map((time) => time.trim())
        .filter(Boolean);

  return [...new Set(times)].sort().map((time) => ({
    time,
    completed: false,
  }));
}
