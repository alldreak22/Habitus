export default function IconButton({
  active = false,
  className = '',
  danger = false,
  icon,
  label,
  type = 'button',
  ...props
}) {
  const classNames = [
    'icon-button',
    active ? 'active' : '',
    danger ? 'danger' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <button className={classNames} type={type} aria-label={label} {...props}>
      <span className="material-symbols-outlined" aria-hidden="true">
        {icon}
      </span>
    </button>
  );
}
