import { useNavigate } from 'react-router-dom';
import AuthPlaceholder from '../components/AuthPlaceholder.jsx';

export default function LoginPage() {
  const navigate = useNavigate();

  function handleDevLogin() {
    const token = import.meta.env.VITE_DEV_AUTH_TOKEN ?? '';
    if (!token) {
      return;
    }
    window.localStorage.setItem('habitus-auth-token', token);
    navigate('/calendario', { replace: true });
  }

  return (
    <AuthPlaceholder
      title="Entrar"
      description="Tela de login preparada para receber o HTML base."
      actionLabel="Entrar"
      onAction={handleDevLogin}
    />
  );
}
