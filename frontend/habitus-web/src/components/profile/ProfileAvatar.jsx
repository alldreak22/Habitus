export default function ProfileAvatar({ imageUrl, name }) {
  if (imageUrl) {
    return <img className="profile-avatar" src={imageUrl} alt={`Foto de perfil de ${name}`} />;
  }

  return (
    <div className="profile-avatar profile-avatar-fallback" aria-label={`Perfil de ${name}`}>
      <span className="material-symbols-outlined" aria-hidden="true">
        person
      </span>
    </div>
  );
}
