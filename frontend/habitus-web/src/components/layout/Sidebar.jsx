import { useEffect, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import appChrome from '../../content/appChrome.json';
import { getProfileOverview, PROFILE_UPDATED_EVENT } from '../../services/profileService.js';
import ProfileAvatar from '../profile/ProfileAvatar.jsx';

const { brand, currentUser, navigationItems } = appChrome;

export default function Sidebar() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(currentUser);

  useEffect(() => {
    getProfileOverview().then((overview) => setProfile(overview.profile));

    function handleProfileUpdated(event) {
      setProfile(event.detail);
    }

    window.addEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);

    return () => {
      window.removeEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);
    };
  }, []);

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
      <div className="sidebar-footer">
        <button className="sidebar-user" type="button" onClick={() => navigate('/perfil')}>
          <ProfileAvatar imageUrl={profile.imageUrl} name={profile.nickname} />
          <div className="sidebar-user-details">
            <p>{profile.nickname}</p>
            <span>{profile.email}</span>
          </div>
        </button>
        <p className="sidebar-version">v0.1.0</p>
      </div>
    </aside>
  );
}
