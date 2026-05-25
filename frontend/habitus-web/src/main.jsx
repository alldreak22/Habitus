import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './app/App.jsx';
import Toast from './components/Toast.jsx';
import { ToastProvider } from './context/ToastContext.jsx';
import './styles/global.css';

const storedTheme = window.localStorage.getItem('habitus-theme');
document.documentElement.dataset.theme =
  storedTheme === 'dark' ||
  (storedTheme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)
    ? 'dark'
    : 'light';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ToastProvider>
      <BrowserRouter>
        <App />
      </BrowserRouter>
      <Toast />
    </ToastProvider>
  </React.StrictMode>,
);
