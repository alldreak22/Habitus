import { Link } from 'react-router-dom';

export default function AuthPlaceholder({ title, description, actionLabel }) {
  return (
    <main className="auth-page">
      <section className="auth-panel">
        <p className="eyebrow">Habitus</p>
        <h1>{title}</h1>
        <p>{description}</p>
        <form className="auth-form">
          <label>
            Email
            <input type="email" name="email" autoComplete="email" />
          </label>
          <label>
            Senha
            <input type="password" name="password" autoComplete="current-password" />
          </label>
          <button type="button">{actionLabel}</button>
        </form>
        <Link to="/">Voltar para o resumo</Link>
      </section>
    </main>
  );
}
