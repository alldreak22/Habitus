export default function SettingsSection({ children, icon, title }) {
  return (
    <section className="settings-section" aria-labelledby={`${title.toLowerCase()}-settings`}>
      <header>
        <span className="material-symbols-outlined" aria-hidden="true">
          {icon}
        </span>
        <h2 id={`${title.toLowerCase()}-settings`}>{title}</h2>
      </header>
      <div className="settings-section-body">{children}</div>
    </section>
  );
}
