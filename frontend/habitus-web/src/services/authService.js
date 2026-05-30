import { API_BASE_URL } from './api.js';
import { PROFILE_UPDATED_EVENT } from './profileService.js';

const AUTH_TOKEN_KEY = 'habitus-auth-token';
const USER_STORAGE_KEY = 'habitus-user';

export async function loginUser({ login, password }) {
  const auth = await requestAuth('/auth/login', { login, password });
  persistAuthenticatedUser(auth);
  return auth;
}

export async function registerUser({ email, name, nick, password }) {
  const auth = await requestAuth('/auth/register', { email, name, nick, password });
  persistAuthenticatedUser(auth);
  return auth;
}

async function requestAuth(path, payload) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error('Falha de conexao com a API.');
  }

  if (!response.ok) {
    let errorMessage = `Erro na API (${response.status}).`;
    try {
      const errorBody = await response.json();
      errorMessage = formatApiError(errorBody, errorMessage);
    } catch {
      // keep fallback message
    }
    throw new Error(errorMessage);
  }

  return response.json();
}

function persistAuthenticatedUser(auth) {
  if (auth?.token) {
    window.localStorage.setItem(AUTH_TOKEN_KEY, auth.token);
  }

  if (!auth?.user) {
    return;
  }

  const profile = {
    email: auth.user.email ?? '',
    imageUrl: auth.user.picture ?? null,
    memberSince: '-',
    name: auth.user.name ?? '',
    nickname: auth.user.nick ?? auth.user.name ?? '',
  };
  window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(profile));
  window.dispatchEvent(new CustomEvent(PROFILE_UPDATED_EVENT, { detail: profile }));
}

function formatApiError(errorBody, fallbackMessage) {
  if (errorBody?.fields && typeof errorBody.fields === 'object') {
    const fieldMessages = Object.entries(errorBody.fields)
      .map(([field, message]) => `${formatFieldName(field)}: ${message}`)
      .join(' ');
    if (fieldMessages) {
      return fieldMessages;
    }
  }

  return errorBody?.message || fallbackMessage;
}

function formatFieldName(field) {
  return {
    email: 'E-mail',
    login: 'Login',
    name: 'Nome',
    nick: 'Usuario',
    password: 'Senha',
  }[field] ?? field;
}
