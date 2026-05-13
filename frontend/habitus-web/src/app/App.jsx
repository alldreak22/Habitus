import { Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from '../layouts/AppLayout.jsx';
import CalendarPage from '../pages/CalendarPage.jsx';
import EvolutionPage from '../pages/EvolutionPage.jsx';
import HabitsPage from '../pages/HabitsPage.jsx';
import LoginPage from '../pages/LoginPage.jsx';
import ProfilePage from '../pages/ProfilePage.jsx';
import RegisterPage from '../pages/RegisterPage.jsx';
import SettingsPage from '../pages/SettingsPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cadastro" element={<RegisterPage />} />
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/calendario" replace />} />
        <Route path="/calendario" element={<CalendarPage />} />
        <Route path="/habitos" element={<HabitsPage />} />
        <Route path="/evolucao" element={<EvolutionPage />} />
        <Route path="/perfil" element={<ProfilePage />} />
        <Route path="/configuracoes" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/calendario" replace />} />
    </Routes>
  );
}
