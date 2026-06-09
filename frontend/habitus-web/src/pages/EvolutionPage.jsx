import { useEffect, useMemo, useState } from 'react';
import TopBar from '../components/layout/TopBar.jsx';
import { getEvolutionStats } from '../services/statsService.js';

const DAYS = 30;

export default function EvolutionPage() {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function loadStats() {
      setIsLoading(true);
      setErrorMessage('');

      try {
        const loadedStats = await getEvolutionStats(DAYS);
        if (isMounted) {
          setStats(loadedStats);
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || 'Nao foi possivel carregar as metricas.');
          setStats(null);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadStats();

    return () => {
      isMounted = false;
    };
  }, []);

  const maxPlannedUnits = useMemo(() => {
    return Math.max(...(stats?.dailySeries ?? []).map((day) => day.plannedUnits), 1);
  }, [stats]);

  const summary = stats?.summary;

  return (
    <>
      <TopBar title="Evolução" />
      <main className="content-area">
        <section className="evolution-page" aria-labelledby="evolution-heading">
          <header className="standard-page-header evolution-header">
            <div>
              <h1 id="evolution-heading">Evolução</h1>
              <p>{periodLabel(stats?.period)}</p>
            </div>
          </header>

          {isLoading ? (
            <p className="empty-state evolution-status">Carregando metricas...</p>
          ) : null}

          {!isLoading && errorMessage ? (
            <section className="evolution-error" role="status">
              <span className="material-symbols-outlined" aria-hidden="true">
                monitoring
              </span>
              <div>
                <h2>Serviço de métricas indisponível</h2>
                <p>{errorMessage}</p>
              </div>
            </section>
          ) : null}

          {!isLoading && summary ? (
            <>
              <section className="evolution-metrics" aria-label="Resumo de evolução">
                <MetricCard
                  icon="check_circle"
                  label="Conclusão"
                  value={`${formatNumber(summary.completionRate)}%`}
                  detail={`${summary.completedUnits}/${summary.plannedUnits} unidades`}
                />
                <MetricCard
                  icon="local_fire_department"
                  label="Sequência atual"
                  value={`${summary.currentStreak}`}
                  detail="dias completos"
                />
                <MetricCard
                  icon="emoji_events"
                  label="Melhor sequência"
                  value={`${summary.bestStreak}`}
                  detail="dias completos"
                />
                <MetricCard
                  icon="target"
                  label="Hábitos ativos"
                  value={`${summary.activeHabits}`}
                  detail="na rotina atual"
                />
              </section>

              <section className="evolution-panel" aria-labelledby="daily-series-heading">
                <header>
                  <div>
                    <h2 id="daily-series-heading">Últimos {DAYS} dias</h2>
                    <p>Conclusões registradas em relação ao planejado.</p>
                  </div>
                </header>
                <div className="evolution-chart" role="list">
                  {(stats.dailySeries ?? []).map((day) => (
                    <DailyBar key={day.date} day={day} maxPlannedUnits={maxPlannedUnits} />
                  ))}
                </div>
              </section>
            </>
          ) : null}
        </section>
      </main>
    </>
  );
}

function MetricCard({ detail, icon, label, value }) {
  return (
    <article className="evolution-metric-card">
      <span className="material-symbols-outlined" aria-hidden="true">
        {icon}
      </span>
      <div>
        <p>{label}</p>
        <strong>{value}</strong>
        <small>{detail}</small>
      </div>
    </article>
  );
}

function DailyBar({ day, maxPlannedUnits }) {
  const plannedHeight = Math.max(8, (day.plannedUnits / maxPlannedUnits) * 100);
  const completedHeight = day.plannedUnits
    ? Math.max(4, (day.completedUnits / maxPlannedUnits) * 100)
    : 0;

  return (
    <article className="evolution-day" role="listitem" title={dayTitle(day)}>
      <div className="evolution-day-bar" style={{ '--planned-height': `${plannedHeight}%` }}>
        <span style={{ '--completed-height': `${completedHeight}%` }} />
      </div>
      <small>{formatDay(day.date)}</small>
    </article>
  );
}

function periodLabel(period) {
  if (!period?.startDate || !period?.endDate) {
    return `Métricas reais dos últimos ${DAYS} dias.`;
  }

  return `${formatDate(period.startDate)} a ${formatDate(period.endDate)}`;
}

function dayTitle(day) {
  return `${formatDate(day.date)}: ${day.completedUnits}/${day.plannedUnits} unidades (${formatNumber(day.completionRate)}%)`;
}

function formatNumber(value) {
  return Number(value ?? 0).toLocaleString('pt-BR', {
    maximumFractionDigits: 1,
  });
}

function formatDate(value) {
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR');
}

function formatDay(value) {
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
  });
}
