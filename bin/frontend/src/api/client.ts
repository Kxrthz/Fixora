const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';

export class ApiError extends Error {
  constructor(message: string, public readonly status: number) { super(message); }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('fixora.accessToken');
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers }
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: 'Something went wrong.' }));
    throw new ApiError(body.message ?? 'Something went wrong.', response.status);
  }
  return response.status === 204 ? undefined as T : response.json() as Promise<T>;
}

