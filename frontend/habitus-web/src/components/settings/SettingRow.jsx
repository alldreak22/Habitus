export default function SettingRow({ children, description, title }) {
  return (
    <div className="setting-row">
      <div>
        <p>{title}</p>
        <span>{description}</span>
      </div>
      {children}
    </div>
  );
}
