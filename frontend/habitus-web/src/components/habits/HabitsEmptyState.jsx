import Button from '../Button.jsx';

export default function HabitsEmptyState({ onCreate }) {
  return (
    <div className="empty-state habits-empty-state">
      <p>Nao foi encontrado nenhum habito. Cadastre um novo para comecar.</p>
      {typeof onCreate === 'function' ? (
        <Button icon="add" onClick={onCreate}>
          Novo habito
        </Button>
      ) : null}
    </div>
  );
}
