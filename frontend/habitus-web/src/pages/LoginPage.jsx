import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useToast } from '../context/ToastContext.jsx';
import { loginUser } from '../services/authService.js';

export default function LoginPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    setIsSubmitting(true);

    try {
      await loginUser({
        login: String(formData.get('login') ?? '').trim(),
        password: String(formData.get('password') ?? ''),
      });
      showToast({ message: 'Login realizado com sucesso.', type: 'success' });
      navigate('/calendario', { replace: true });
    } catch (error) {
      showToast({ message: error.message || 'Nao foi possivel entrar.', type: 'warning' });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page auth-page-login">
      <div className="auth-accent-bar" aria-hidden="true" />
      <section className="auth-stack auth-stack-compact" aria-labelledby="login-title">
        <AuthBrand showIcon />

        <div className="auth-card">
          <form className="auth-form-v2" onSubmit={handleLogin}>
            <AuthField
              autoComplete="username"
              icon="mail"
              id="login"
              label="E-mail ou Usuario"
              name="login"
              placeholder="exemplo@email.com"
              required
              type="text"
            />

            <AuthField
              action={<Link to="/recuperar-senha">Esqueci senha</Link>}
              autoComplete="current-password"
              icon="lock"
              id="password"
              label="Senha"
              name="password"
              onToggleVisibility={() => setIsPasswordVisible((currentValue) => !currentValue)}
              placeholder="********"
              required
              trailingIcon={isPasswordVisible ? 'visibility_off' : 'visibility'}
              type={isPasswordVisible ? 'text' : 'password'}
            />

            <button className="auth-primary-action" disabled={isSubmitting} type="submit">
              {isSubmitting ? 'Entrando...' : 'Entrar'}
              <span className="material-symbols-outlined" aria-hidden="true">
                arrow_forward
              </span>
            </button>
          </form>

          <div className="auth-card-footer">
            <p>Novo por aqui?</p>
            <Link className="auth-secondary-action" to="/cadastro">
              Criar novo cadastro
            </Link>
          </div>
        </div>

        <footer className="auth-footer">
          <p>{'\u00a9'} 2026 Habitus.</p>
        </footer>
      </section>
    </main>
  );
}

function AuthBrand({ showIcon = false }) {
  return (
    <header className="auth-brand">
      {showIcon ? (
        <div className="auth-brand-icon">
          <span className="material-symbols-outlined filled" aria-hidden="true">
            check_circle
          </span>
        </div>
      ) : null}
      <h1 id="login-title">Habitus</h1>
      <p>Produtividade Serena</p>
    </header>
  );
}

function AuthField({
  action,
  autoComplete,
  icon,
  id,
  label,
  name,
  onToggleVisibility,
  placeholder,
  required = false,
  trailingIcon,
  type,
}) {
  return (
    <label className="auth-field" htmlFor={id}>
      <span className="auth-field-label">
        <span>{label}</span>
        {action ? <span className="auth-field-action">{action}</span> : null}
      </span>
      <span className="auth-input-wrap">
        {icon ? (
          <span className="material-symbols-outlined auth-input-icon" aria-hidden="true">
            {icon}
          </span>
        ) : null}
        <input
          autoComplete={autoComplete}
          id={id}
          name={name}
          placeholder={placeholder}
          required={required}
          type={type}
        />
        {trailingIcon ? (
          <button
            className="auth-input-button"
            type="button"
            aria-label={type === 'password' ? 'Mostrar senha' : 'Ocultar senha'}
            onClick={onToggleVisibility}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              {trailingIcon}
            </span>
          </button>
        ) : null}
      </span>
    </label>
  );
}
