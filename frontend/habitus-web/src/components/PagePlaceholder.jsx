import TopBar from './layout/TopBar.jsx';

export default function PagePlaceholder({ title, description }) {
  return (
    <>
      <TopBar />
      <main className="content-area">
        <section className="standard-page">
          <header className="standard-page-header">
            <div>
              <h1>{title}</h1>
              <p>{description}</p>
            </div>
          </header>
          <div className="placeholder-card">
            <span className="material-symbols-outlined" aria-hidden="true">
              construction
            </span>
            <p>Conteúdo em desenvolvimento.</p>
          </div>
        </section>
      </main>
    </>
  );
}
