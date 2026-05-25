import { useEffect } from 'react';
import { useToast } from '../context/ToastContext.jsx';

export default function Toast() {
  const { dismissToast, toast } = useToast();
  const message = toast?.message;
  const type = toast?.type ?? 'warning';
  const duration = toast?.duration ?? (type === 'success' ? 1000 : 2000);

  useEffect(() => {
    if (!message) {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      dismissToast();
    }, duration);

    return () => window.clearTimeout(timeoutId);
  }, [dismissToast, duration, message]);

  if (!message) {
    return null;
  }

  return (
    <button
      className={`toast toast-${type}`}
      type="button"
      role="status"
      aria-live="polite"
      onClick={dismissToast}
    >
      <span className="toast-message">{message}</span>
      <span
        key={toast?.id}
        className="toast-progress"
        aria-hidden="true"
        style={{ animationDuration: `${duration}ms` }}
      />
    </button>
  );
}
