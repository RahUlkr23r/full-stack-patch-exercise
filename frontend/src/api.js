const API_BASE = '/api';

export async function fetchTasks({ query = '', status = '', page = 1, pageSize = 10 }) {
  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (status) params.set('status', status);
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));

  const url = `${API_BASE}/tasks?${params.toString()}`;
  console.log('[api] fetching:', url);

  const response = await fetch(url);

  if (!response.ok) {
    let errorMessage = `Request failed: ${response.status}`;
    try {
      const data = await response.json();
      if (data && data.error) {
        errorMessage = data.error;
      }
    } catch (_) {
      // Fallback to HTTP status if body is not JSON
    }
    throw new Error(errorMessage);
  }

  return response.json();
}
