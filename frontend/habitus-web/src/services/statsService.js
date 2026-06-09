const STATS_API_BASE_URL = import.meta.env.VITE_STATS_API_BASE_URL ?? 'http://localhost:5090/api';
const DEV_AUTH_TOKEN = import.meta.env.VITE_DEV_AUTH_TOKEN ?? '';

export async function statsRequest(path, options = {}) {
  const authToken = window.localStorage.getItem('habitus-auth-token') || DEV_AUTH_TOKEN;
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  let response;
  try {
    response = await fetch(`${STATS_API_BASE_URL}${path}`, {
      ...options,
      headers,
    });
  } catch (error) {
    throw new Error('Falha de conexao com o servico de metricas.');
  }

  if (!response.ok) {
    let message = `Erro no servico de metricas (${response.status}).`;
    try {
      const body = await response.json();
      if (body?.message) {
        message = body.message;
      }
    } catch (error) {
      // keep fallback message
    }
    const requestError = new Error(message);
    requestError.status = response.status;
    throw requestError;
  }

  return response.status === 204 ? null : response.json();
}

export function getEvolutionStats(days = 30) {
  return statsRequest(`/stats/evolution?days=${days}`);
}

export { STATS_API_BASE_URL };
