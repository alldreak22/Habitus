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
    if (editorRef.current) {
      editorRef.current.innerHTML = editorHtml || '';
    }
  }, []);

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

  function togglePlannedHabit(habitId) {
    setPlannedHabits((currentHabits) =>
      currentHabits.map((habit) =>
        habit.id === habitId ? { ...habit, completed: !habit.completed } : habit,
      ),
    );
  }

  function handleAddHabits(nextHabits) {
    const existingIds = new Set(plannedHabits.map((habit) => habit.id));
    const habitsToAdd = nextHabits
      .filter((habit) => !existingIds.has(habit.id))
      .map((habit) => ({
        ...habit,
        completed: false,
        detail: habit.description,
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
              {plannedHabits.map((habit) => (
                <button
                  key={habit.id}
                  className={habit.completed ? 'day-habit-item completed' : 'day-habit-item'}
                  type="button"
                  onClick={() => togglePlannedHabit(habit.id)}
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
                  <span>{habit.name}</span>
                </button>
              ))}
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
