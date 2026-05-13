export default function Button({
  children,
  className = '',
  fullWidth = false,
  icon,
  type = 'button',
  variant = 'primary',
  ...props
}) {
  const classNames = ['button', `button-${variant}`, fullWidth ? 'button-full' : '', className]
    .filter(Boolean)
    .join(' ');

  return (
    <button className={classNames} type={type} {...props}>
      {icon ? (
        <span className="material-symbols-outlined" aria-hidden="true">
          {icon}
        </span>
      ) : null}
      {children}
    </button>
  );
}
