import { useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import Button from '../Button.jsx';
import appChrome from '../../content/appChrome.json';
import { logoutUser } from '../../services/authService.js';
import { getProfileOverview, PROFILE_UPDATED_EVENT } from '../../services/profileService.js';
import ProfileAvatar from '../profile/ProfileAvatar.jsx';

const { brand, navigationItems } = appChrome;
const emptyUser = { email: '', imageUrl: null, nickname: '' };

export default function Sidebar() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(emptyUser);
  const [isLogoutDialogOpen, setIsLogoutDialogOpen] = useState(false);

  useEffect(() => {
    getProfileOverview()
      .then((overview) => setProfile(overview.profile))
      .catch(() => {});

    function handleProfileUpdated(event) {
      setProfile(event.detail);
    }

    window.addEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);

    return () => {
      window.removeEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);
    };
  }, []);

  function handleLogout(event) {
    event.stopPropagation();
    setIsLogoutDialogOpen(true);
  }

  function confirmLogout() {
    logoutUser();
    navigate('/login', { replace: true });
  }

  return (
    <>
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
      <div className="sidebar-footer">
        <div className="sidebar-user-card">
          <button className="sidebar-user" type="button" onClick={() => navigate('/perfil')}>
            <ProfileAvatar imageUrl={profile.imageUrl} name={profile.nickname} />
            <div className="sidebar-user-details">
              <p>{profile.nickname}</p>
              <span>{profile.email}</span>
            </div>
          </button>
          <button className="sidebar-logout-button" type="button" aria-label="Sair da conta" onClick={handleLogout}>
            <span className="material-symbols-outlined" aria-hidden="true">
              logout
            </span>
          </button>
        </div>
      </div>
      </aside>
      {isLogoutDialogOpen ? (
        <div className="dialog-backdrop" role="presentation">
          <section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="logout-dialog-title">
            <h2 id="logout-dialog-title">Sair da conta?</h2>
            <p>Confirme se deseja encerrar a sessão atual e voltar para a tela de login.</p>
            <footer>
              <Button variant="secondary" onClick={() => setIsLogoutDialogOpen(false)}>
                Cancelar
              </Button>
              <Button onClick={confirmLogout}>Sair</Button>
            </footer>
          </section>
        </div>
      ) : null}
    </>
  );
}
