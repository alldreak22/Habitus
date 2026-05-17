export default function ToggleSwitch({ checked, label, onChange }) {
  return (
    <button
      className={checked ? 'settings-toggle checked' : 'settings-toggle'}
      type="button"
      aria-pressed={checked}
      aria-label={label}
      onClick={() => onChange(!checked)}
    >
      <span aria-hidden="true" />
    </button>
  );
}
