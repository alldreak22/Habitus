import { createContext, useCallback, useContext, useMemo, useState } from 'react';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toast, setToast] = useState(null);

  const dismissToast = useCallback(() => {
    setToast(null);
  }, []);

  const showToast = useCallback(({ message, type = 'warning', duration } = {}) => {
    if (!message) {
      return;
    }

    setToast({
      duration,
      id: Date.now(),
      message,
      type,
    });
  }, []);

  const value = useMemo(
    () => ({
      dismissToast,
      showToast,
      toast,
    }),
    [dismissToast, showToast, toast],
  );

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>;
}

export function useToast() {
  const context = useContext(ToastContext);

  if (!context) {
    throw new Error('useToast must be used within ToastProvider');
  }

  return context;
}
