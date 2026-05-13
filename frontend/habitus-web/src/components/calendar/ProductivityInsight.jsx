import TipCard from '../TipCard.jsx';

export default function ProductivityInsight({ insights }) {
  return (
    <TipCard
      icon="lightbulb"
      items={insights.length ? insights : ['Carregando insight de produtividade...']}
      title="Insight de Produtividade"
    />
  );
}
