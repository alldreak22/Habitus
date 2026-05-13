import { useEffect, useMemo, useState } from 'react';

export default function TipCard({ icon = 'auto_awesome', items, refreshKey = 0, title }) {
  const tips = useMemo(() => items.filter(Boolean), [items]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isDismissed, setIsDismissed] = useState(false);

  useEffect(() => {
    if (!tips.length) {
      return;
    }

    setCurrentIndex(Math.floor(Math.random() * tips.length));
    setIsDismissed(false);
  }, [refreshKey, tips]);

  if (isDismissed || !tips.length) {
    return null;
  }

  function handlePrevious() {
    setCurrentIndex((index) => (index === 0 ? tips.length - 1 : index - 1));
  }

  function handleNext() {
    setCurrentIndex((index) => (index + 1) % tips.length);
  }

  return (
    <section className="tip-card">
      <span className="material-symbols-outlined tip-icon" aria-hidden="true">
        {icon}
      </span>
      <div className="tip-card-copy">
        <h5>{title}</h5>
        <p>{tips[currentIndex]}</p>
      </div>
      {tips.length > 1 && (
        <div className="tip-card-actions" aria-label={`Navegar ${title.toLowerCase()}`}>
          <button type="button" onClick={handlePrevious} aria-label="Dica anterior">
            <span className="material-symbols-outlined" aria-hidden="true">
              chevron_left
            </span>
          </button>
          <button type="button" onClick={handleNext} aria-label="Proxima dica">
            <span className="material-symbols-outlined" aria-hidden="true">
              chevron_right
            </span>
          </button>
        </div>
      )}
      <button
        className="tip-dismiss"
        type="button"
        onClick={() => setIsDismissed(true)}
        aria-label={`Dispensar ${title.toLowerCase()}`}
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          close
        </span>
      </button>
    </section>
  );
}
