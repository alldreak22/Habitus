import { Link } from 'react-router-dom';

export default function ResetPasswordPage() {
  return (
    <main className="auth-page">
      <section className="auth-stack" aria-labelledby="reset-title">
        <header className="auth-brand">
          <div className="auth-brand-icon">
            <span className="material-symbols-outlined filled" aria-hidden="true">
              check_circle
            </span>
          </div>
          <h1>Habitus</h1>
          <p>Produtividade Serena</p>
        </header>

        <div className="auth-card">
          <header className="auth-card-heading">
            <h2 id="reset-title">Recuperar senha</h2>
            <p>Informe seu e-mail para receber as instrucoes de redefinicao.</p>
          </header>

          <form className="auth-form-v2">
            <AuthField
              autoComplete="username"
              icon="mail"
              id="identifier"
              label="E-mail ou usuario"
              placeholder="exemplo@habitus.com"
              type="text"
            />

            <button className="auth-primary-action" type="submit">
              Enviar link de recuperacao
              <span className="material-symbols-outlined" aria-hidden="true">
                arrow_forward
              </span>
            </button>
          </form>

          <div className="auth-card-divider" />

          <div className="auth-card-footer auth-card-footer-link">
            <Link to="/login">
              <span className="material-symbols-outlined" aria-hidden="true">
                arrow_back
              </span>
              Voltar para o login
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

function AuthField({ autoComplete, icon, id, label, placeholder, type }) {
  return (
    <label className="auth-field" htmlFor={id}>
      <span className="auth-field-label">
        <span>{label}</span>
      </span>
      <span className="auth-input-wrap">
        <span className="material-symbols-outlined auth-input-icon" aria-hidden="true">
          {icon}
        </span>
        <input autoComplete={autoComplete} id={id} placeholder={placeholder} type={type} />
      </span>
    </label>
  );
}
