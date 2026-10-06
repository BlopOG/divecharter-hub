// One place for every call to the Spring Boot API.
const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// Set by AuthProvider: called when a logged-in request comes back 401 (expired/invalid token)
let unauthorizedHandler = null;

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

export async function apiRequest(path, { method = "GET", body, token } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // 204 No Content (e.g. after a delete) has no body to read
  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    // A request that SENT a token was rejected → the session is no longer valid
    if (response.status === 401 && token && unauthorizedHandler) {
      unauthorizedHandler();
    }
    // Your backend always returns { status, message, fieldErrors? } on errors
    const error = new Error(data?.message ?? `Request failed (${response.status})`);
    error.status = response.status;
    error.fieldErrors = data?.fieldErrors ?? {};
    throw error;
  }

  return data;
}