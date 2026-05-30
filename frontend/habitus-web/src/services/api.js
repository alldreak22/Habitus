const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';
const DEV_AUTH_TOKEN = import.meta.env.VITE_DEV_AUTH_TOKEN ?? '';

export async function apiRequest(path, options = {}) {
  const authToken = window.localStorage.getItem('habitus-auth-token') || DEV_AUTH_TOKEN;
  const baseHeaders = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  if (authToken) {
    baseHeaders.Authorization = `Bearer ${authToken}`;
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: baseHeaders,
    });
  } catch (error) {
    throw new Error('Falha de conexao com a API.');
  }

  if (!response.ok) {
    let errorMessage = `Erro na API (${response.status}).`;
    try {
      const errorBody = await response.json();
      if (errorBody?.message) {
        errorMessage = errorBody.message;
      }
    } catch (error) {
      // keep fallback message
    }
    throw new Error(errorMessage);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export { API_BASE_URL };
