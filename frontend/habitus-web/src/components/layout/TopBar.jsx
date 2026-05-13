import { useState } from 'react';
import Button from '../Button.jsx';

export default function TopBar({
  notifications = [],
  onPrimaryAction,
  onSearchChange,
  primaryActionIcon = 'add',
  primaryActionLabel,
  searchPlaceholder = 'Filtrar',
  searchValue,
  title,
}) {
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const hasSearch = typeof onSearchChange === 'function';
  const hasNotifications = notifications.length > 0;
  const isActionsOnly = !title && !hasSearch;

  return (
    <header className="topbar">
      <div className={isActionsOnly ? 'topbar-inner topbar-inner-actions-only' : 'topbar-inner'}>
        {title && !hasSearch ? <h2>{title}</h2> : null}
        {hasSearch ? (
          <label className="topbar-search">
            <span className="material-symbols-outlined" aria-hidden="true">
              search
            </span>
            <input
              type="search"
              value={searchValue}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder={searchPlaceholder}
            />
          </label>
        ) : null}
        <div className="topbar-actions">
          <div className="notification-anchor">
            <button
              className="notification-button"
              type="button"
              aria-expanded={isNotificationsOpen}
              aria-label="Notificações"
              onClick={() => setIsNotificationsOpen((isOpen) => !isOpen)}
            >
              <span className="material-symbols-outlined" aria-hidden="true">
                notifications
              </span>
              {hasNotifications ? <span className="notification-dot" aria-hidden="true" /> : null}
            </button>
            {isNotificationsOpen ? (
              <div className="notification-popover" role="dialog" aria-label="Notificações">
                <h3>Notificações</h3>
                {hasNotifications ? (
                  <ul className="notification-list">
                    {notifications.map((notification) => (
                      <li key={notification.id}>{notification.text}</li>
                    ))}
                  </ul>
                ) : (
                  <p>Nenhuma notificação nova por enquanto.</p>
                )}
              </div>
            ) : null}
          </div>
          {primaryActionLabel ? (
            <Button icon={primaryActionIcon} onClick={onPrimaryAction}>
              {primaryActionLabel}
            </Button>
          ) : null}
        </div>
      </div>
    </header>
  );
}
