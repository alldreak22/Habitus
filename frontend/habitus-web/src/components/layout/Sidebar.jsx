import { useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import appChrome from '../../content/appChrome.json';
import IconButton from '../IconButton.jsx';
import ProfileAvatar from '../profile/ProfileAvatar.jsx';

const { brand, currentUser, navigationItems } = appChrome;

export default function Sidebar() {
  const navigate = useNavigate();
  const [theme, setTheme] = useState(() =>
    document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light',
  );

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('habitus-theme', theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'));
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <h1>{brand.name}</h1>
        <p>{brand.tagline}</p>
      </div>
      <nav className="sidebar-nav" aria-label="Navegação principal">
        {navigationItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => (isActive ? 'sidebar-link active' : 'sidebar-link')}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              {item.icon}
            </span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-user">
        <ProfileAvatar imageUrl={currentUser.imageUrl} name={currentUser.name} />
        <div className="sidebar-user-details">
          <p>{currentUser.name}</p>
          <div className="sidebar-user-actions" aria-label="Ações do usuário">
            <IconButton
              icon={theme === 'dark' ? 'light_mode' : 'dark_mode'}
              label={theme === 'dark' ? 'Ativar tema claro' : 'Ativar tema escuro'}
              onClick={toggleTheme}
            />
            <IconButton
              icon="settings"
              label="Abrir configurações"
              onClick={() => navigate('/configuracoes')}
            />
            <IconButton icon="account_circle" label="Abrir perfil" onClick={() => navigate('/perfil')} />
          </div>
        </div>
      </div>
    </aside>
  );
}
