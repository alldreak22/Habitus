import { useEffect, useMemo, useRef, useState } from 'react';
import Button from '../components/Button.jsx';
import IconButton from '../components/IconButton.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import ProfileAvatar from '../components/profile/ProfileAvatar.jsx';
import appChrome from '../content/appChrome.json';
import { getProfileOverview, saveProfile } from '../services/profileService.js';

const { currentUser } = appChrome;

export default function ProfilePage() {
  const fileInputRef = useRef(null);
  const [profileOverview, setProfileOverview] = useState(null);
  const [savedProfile, setSavedProfile] = useState(currentUser);
  const [profileForm, setProfileForm] = useState(currentUser);
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);

  useEffect(() => {
    getProfileOverview().then((overview) => {
      setProfileOverview(overview);
      setSavedProfile(overview.profile);
      setProfileForm(overview.profile);
    });
  }, []);

  const security = profileOverview?.security;
  const summary = profileOverview?.summary;
  const focusPrompt = profileOverview?.focusPrompt;
  const completionPercentage = summary?.completionPercentage ?? 0;
  const displayName = savedProfile.nickname || savedProfile.name;

  function updateProfileField(field, value) {
    setProfileForm((currentProfile) => ({
      ...currentProfile,
      [field]: value,
    }));
  }

  function handlePhotoChange(event) {
    const [file] = event.target.files;

    if (!file) {
      return;
    }

    const nextImageUrl = URL.createObjectURL(file);
    setProfileForm((currentProfile) => {
      if (currentProfile.imageUrl?.startsWith('blob:')) {
        URL.revokeObjectURL(currentProfile.imageUrl);
      }

      return {
        ...currentProfile,
        imageUrl: nextImageUrl,
      };
    });
  }

  async function handleSaveProfile() {
    const saved = await saveProfile(profileForm);
    setSavedProfile(saved);
  }

  return (
    <>
      <TopBar title="Perfil" />
      <main className="content-area">
        <section className="profile-page" aria-labelledby="profile-heading">
          <div className="profile-main-column">
            <header className="profile-hero">
              <div className="profile-photo-wrap">
                <ProfileAvatar imageUrl={profileForm.imageUrl} name={displayName} />
                <input
                  ref={fileInputRef}
                  className="profile-photo-input"
                  type="file"
                  accept="image/*"
                  onChange={handlePhotoChange}
                />
                <IconButton
                  icon="edit"
                  label="Editar foto de perfil"
                  onClick={() => fileInputRef.current?.click()}
                />
              </div>
              <div>
                <h1 id="profile-heading">{displayName}</h1>
                <p>Membro desde {savedProfile.memberSince}</p>
              </div>
            </header>

            <section className="profile-section" aria-labelledby="personal-data-heading">
              <div className="profile-section-heading">
                <h2 id="personal-data-heading">Dados Pessoais</h2>
              </div>
              <div className="profile-data-grid">
                <ProfileField
                  label="Nome Completo"
                  value={profileForm.name}
                  onChange={(value) => updateProfileField('name', value)}
                />
                <ProfileField
                  label="Apelido"
                  value={profileForm.nickname}
                  onChange={(value) => updateProfileField('nickname', value)}
                />
                <ProfileField
                  label="Endereço de e-mail"
                  type="email"
                  value={profileForm.email}
                  onChange={(value) => updateProfileField('email', value)}
                />
              </div>
            </section>

            <section className="profile-section" aria-labelledby="security-heading">
              <h2 id="security-heading">Segurança</h2>
              <div className="security-row">
                <span className="material-symbols-outlined" aria-hidden="true">
                  lock
                </span>
                <div>
                  <p>Senha</p>
                  <span>{security?.passwordLastChanged ?? 'Carregando...'}</span>
                </div>
                <Button variant="secondary" onClick={() => setIsPasswordModalOpen(true)}>
                  Alterar senha
                </Button>
              </div>
            </section>

            <footer className="profile-actions">
              <Button icon="save" onClick={handleSaveProfile}>
                Salvar alterações
              </Button>
            </footer>
          </div>

          <aside className="profile-side-panel" aria-label="Resumo da conta">
            <section className="account-summary-card">
              <h2>Resumo da Conta</h2>
              <div className="streak-card">
                <span className="material-symbols-outlined filled" aria-hidden="true">
                  local_fire_department
                </span>
                <strong>{summary?.streakDays ?? 0}</strong>
                <p>Dias de sequência</p>
              </div>
              <div className="completion-stat">
                <div>
                  <p>Hábitos concluídos</p>
                  <span>{completionPercentage}%</span>
                </div>
                <div className="progress-meter">
                  <span style={{ width: `${completionPercentage}%` }} />
                </div>
                <small>
                  {summary?.completedGoals ?? 0} de {summary?.totalGoals ?? 0} metas{' '}
                  {summary?.goalsPeriod ?? ''}
                </small>
              </div>
              <div className="achievement-card">
                <span className="material-symbols-outlined" aria-hidden="true">
                  military_tech
                </span>
                <div>
                  <p>{summary?.achievement.title ?? 'Carregando'}</p>
                  <span>{summary?.achievement.description ?? 'Carregando...'}</span>
                </div>
              </div>
              <Button variant="outline" icon="export_notes">
                Exportar relatório PDF
              </Button>
            </section>
            <section className="focus-prompt-card">
              <h2>{focusPrompt?.title ?? 'Produtividade Serena'}</h2>
              <p>{focusPrompt?.description ?? 'Carregando...'}</p>
              <Button variant="secondary">{focusPrompt?.actionLabel ?? 'Iniciar timer'}</Button>
            </section>
          </aside>
        </section>
      </main>
      {isPasswordModalOpen ? <PasswordModal onClose={() => setIsPasswordModalOpen(false)} /> : null}
    </>
  );
}

function ProfileField({ label, onChange, type = 'text', value }) {
  return (
    <label className="profile-field">
      <span>{label}</span>
      <input type={type} value={value ?? ''} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function PasswordModal({ onClose }) {
  const [form, setForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [visibleFields, setVisibleFields] = useState({});

  const strength = useMemo(() => {
    let score = 0;

    if (form.newPassword.length >= 8) score += 1;
    if (/[A-Z]/.test(form.newPassword) && /[a-z]/.test(form.newPassword)) score += 1;
    if (/\d/.test(form.newPassword) || /[^A-Za-z0-9]/.test(form.newPassword)) score += 1;

    return score;
  }, [form.newPassword]);

  function updatePasswordField(field, value) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }));
  }

  function toggleFieldVisibility(field) {
    setVisibleFields((currentFields) => ({
      ...currentFields,
      [field]: !currentFields[field],
    }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    onClose();
  }

  return (
    <div className="dialog-backdrop password-dialog-backdrop" role="presentation">
      <section className="password-dialog" role="dialog" aria-modal="true" aria-labelledby="password-title">
        <header>
          <div>
            <h2 id="password-title">Alterar Senha</h2>
            <p>Sua nova senha deve ter pelo menos 8 caracteres.</p>
          </div>
          <IconButton icon="close" label="Fechar modal" onClick={onClose} />
        </header>

        <form className="password-form" onSubmit={handleSubmit}>
          <PasswordField
            label="Senha atual"
            name="currentPassword"
            value={form.currentPassword}
            visible={visibleFields.currentPassword}
            onChange={updatePasswordField}
            onToggleVisibility={toggleFieldVisibility}
          />
          <PasswordField
            label="Nova senha"
            name="newPassword"
            placeholder="Mínimo 8 caracteres"
            value={form.newPassword}
            visible={visibleFields.newPassword}
            onChange={updatePasswordField}
            onToggleVisibility={toggleFieldVisibility}
          />
          <PasswordField
            label="Confirmar nova senha"
            name="confirmPassword"
            placeholder="Repita a nova senha"
            value={form.confirmPassword}
            visible={visibleFields.confirmPassword}
            onChange={updatePasswordField}
            onToggleVisibility={toggleFieldVisibility}
          />

          <div className="password-strength" aria-label="Segurança da senha">
            <span>
              <span className="material-symbols-outlined" aria-hidden="true">
                info
              </span>
              Segurança da senha
            </span>
            <div>
              {[1, 2, 3, 4].map((level) => (
                <i key={level} className={level <= strength ? 'active' : ''} />
              ))}
            </div>
          </div>

          <footer>
            <Button variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit">Salvar Alterações</Button>
          </footer>
        </form>
      </section>
    </div>
  );
}

function PasswordField({
  label,
  name,
  onChange,
  onToggleVisibility,
  placeholder = '',
  value,
  visible = false,
}) {
  return (
    <label className="password-field">
      <span>{label}</span>
      <div>
        <input
          type={visible ? 'text' : 'password'}
          value={value}
          placeholder={placeholder}
          onChange={(event) => onChange(name, event.target.value)}
        />
        <button type="button" onClick={() => onToggleVisibility(name)} aria-label={`Mostrar ${label}`}>
          <span className="material-symbols-outlined" aria-hidden="true">
            {visible ? 'visibility' : 'visibility_off'}
          </span>
        </button>
      </div>
    </label>
  );
}
