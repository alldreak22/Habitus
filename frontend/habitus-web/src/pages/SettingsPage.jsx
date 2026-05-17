import { useEffect, useState } from 'react';
import SelectDropdown from '../components/SelectDropdown.jsx';
import TopBar from '../components/layout/TopBar.jsx';
import SegmentedSettingControl from '../components/settings/SegmentedSettingControl.jsx';
import SettingRow from '../components/settings/SettingRow.jsx';
import SettingsSection from '../components/settings/SettingsSection.jsx';
import ToggleSwitch from '../components/settings/ToggleSwitch.jsx';

const themeOptions = [
  { label: 'Claro', value: 'light' },
  { label: 'Escuro', value: 'dark' },
  { label: 'Sistema', value: 'system' },
];

const languageOptions = [
  { label: 'Português (Brasil)', value: 'pt-BR' },
  { label: 'English (US)', value: 'en-US' },
];

function resolveTheme(themePreference) {
  if (themePreference === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  return themePreference;
}

export default function SettingsPage() {
  const [settings, setSettings] = useState(() => ({
    dailyBrowserReminders: true,
    language: window.localStorage.getItem('habitus-language') ?? 'pt-BR',
    theme: window.localStorage.getItem('habitus-theme') ?? 'light',
    weeklyEmailReports: true,
  }));

  useEffect(() => {
    document.documentElement.dataset.theme = resolveTheme(settings.theme);
    window.localStorage.setItem('habitus-theme', settings.theme);
  }, [settings.theme]);

  useEffect(() => {
    window.localStorage.setItem('habitus-language', settings.language);
  }, [settings.language]);

  function updateSetting(field, value) {
    setSettings((currentSettings) => ({
      ...currentSettings,
      [field]: value,
    }));
  }

  return (
    <>
      <TopBar />
      <main className="content-area">
        <section className="settings-page" aria-labelledby="settings-heading">
          <div className="settings-main-column">
            <header className="settings-header">
              <div>
                <h1 id="settings-heading">Configurações</h1>
                <p>Ajuste preferências de interface e canais de notificação.</p>
              </div>
            </header>

            <SettingsSection icon="palette" title="Interface">
              <SettingRow title="Tema Visual" description="Escolha como o Habitus aparece para você">
                <SegmentedSettingControl
                  label="Tema visual"
                  options={themeOptions}
                  value={settings.theme}
                  onChange={(value) => updateSetting('theme', value)}
                />
              </SettingRow>

              <SettingRow title="Idioma" description="Selecione o idioma da interface">
                <SelectDropdown
                  label="Idioma"
                  options={languageOptions}
                  value={settings.language}
                  onChange={(value) => updateSetting('language', value)}
                />
              </SettingRow>
            </SettingsSection>

            <SettingsSection icon="notifications" title="Notificações">
              <SettingRow
                title="Lembretes Diários"
                description="Receba alertas por notificação do navegador para manter seus hábitos em dia"
              >
                <ToggleSwitch
                  checked={settings.dailyBrowserReminders}
                  label="Ativar lembretes diários por notificação do navegador"
                  onChange={(value) => updateSetting('dailyBrowserReminders', value)}
                />
              </SettingRow>

              <SettingRow
                title="Relatórios Semanais"
                description="Receba por email um resumo do seu desempenho aos domingos"
              >
                <ToggleSwitch
                  checked={settings.weeklyEmailReports}
                  label="Ativar relatórios semanais por email"
                  onChange={(value) => updateSetting('weeklyEmailReports', value)}
                />
              </SettingRow>
            </SettingsSection>
          </div>
        </section>
      </main>
    </>
  );
}
