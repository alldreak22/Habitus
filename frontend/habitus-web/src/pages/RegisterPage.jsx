import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useToast } from '../context/ToastContext.jsx';
import { registerUser } from '../services/authService.js';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const password = String(formData.get('password') ?? '');
    const confirmPassword = String(formData.get('confirmPassword') ?? '');

    if (password !== confirmPassword) {
      showToast({ message: 'As senhas nao conferem.', type: 'warning' });
      return;
    }

    setIsSubmitting(true);
    try {
      await registerUser({
        email: String(formData.get('email') ?? '').trim(),
        name: String(formData.get('name') ?? '').trim(),
        nick: String(formData.get('nick') ?? '').trim(),
        password,
      });
      showToast({ message: 'Cadastro realizado com sucesso.', type: 'success' });
      navigate('/calendario', { replace: true });
    } catch (error) {
      showToast({ message: error.message || 'Nao foi possivel criar o cadastro.', type: 'warning' });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-stack" aria-labelledby="register-title">
        <header className="auth-brand auth-brand-simple">
          <h1>Habitus</h1>
          <p>Produtividade Serena</p>
        </header>

        <div className="auth-card">
          <header className="auth-card-heading">
            <h2 id="register-title">Criar conta</h2>
            <p>Comece sua jornada de produtividade serena.</p>
          </header>

          <form className="auth-form-v2" onSubmit={handleSubmit}>
            <AuthField
              autoComplete="name"
              id="name"
              label="Nome"
              name="name"
              placeholder="Seu nome completo"
              required
              type="text"
            />
            <AuthField
              autoComplete="username"
              id="nick"
              label="Usuario"
              name="nick"
              placeholder="seu_usuario"
              required
              type="text"
            />
            <AuthField
              autoComplete="email"
              id="email"
              label="E-mail"
              name="email"
              placeholder="seu@email.com"
              required
              type="email"
            />
            <AuthField
              autoComplete="new-password"
              id="password"
              label="Senha"
              name="password"
              onToggleVisibility={() => setIsPasswordVisible((currentValue) => !currentValue)}
              placeholder="********"
              required
              trailingIcon={isPasswordVisible ? 'visibility_off' : 'visibility'}
              type={isPasswordVisible ? 'text' : 'password'}
            />
            <AuthField
              autoComplete="new-password"
              id="confirm-password"
              label="Confirmar senha"
              name="confirmPassword"
              placeholder="********"
              required
              type="password"
            />

            <button className="auth-primary-action auth-primary-action-spacious" disabled={isSubmitting} type="submit">
              {isSubmitting ? 'Cadastrando...' : 'Cadastrar'}
            </button>
          </form>

          <div className="auth-card-footer auth-card-footer-inline">
            <p>
              Ja tem uma conta?
              <Link to="/login">Entrar</Link>
            </p>
          </div>
        </div>

        <div className="auth-feature-row" aria-hidden="true">
          <span>
            <i className="material-symbols-outlined">check_circle</i>
            HABITOS
          </span>
          <span>
            <i className="material-symbols-outlined">calendar_today</i>
            CALENDARIO
          </span>
          <span>
            <i className="material-symbols-outlined">spa</i>
            FOCO
          </span>
        </div>
      </section>
    </main>
  );
}

function AuthField({
  autoComplete,
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
      </span>
      <span className="auth-input-wrap">
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
