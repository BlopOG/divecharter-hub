import { useCallback, useEffect, useMemo, useState } from "react";
import { apiRequest, setUnauthorizedHandler } from "../api/client.js";
import { AuthContext } from "./auth-context.js";

const STORAGE_KEY = "divematrix.auth";
const LOGGED_OUT = { token: null, user: null };

function loadStoredAuth() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) ?? LOGGED_OUT;
  } catch {
    return LOGGED_OUT;
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(loadStoredAuth);
  // If a token was saved, we must confirm it's still valid before trusting it
  const [checking, setChecking] = useState(() => Boolean(loadStoredAuth().token));

  const saveAuth = useCallback((next) => {
    setAuth(next);
    if (next.token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  const logout = useCallback(() => saveAuth(LOGGED_OUT), [saveAuth]);

  // Any API call that comes back 401 with a token → log out automatically
  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  // On first load: check a saved token against the backend and refresh the user's details
  useEffect(() => {
    const { token } = loadStoredAuth();
    if (!token) return;

    apiRequest("/api/auth/me", { token })
      .then((user) => saveAuth({ token, user }))
      .catch(() => logout())
      .finally(() => setChecking(false));
  }, [saveAuth, logout]);

  const login = useCallback(
    async (email, password) => {
      const result = await apiRequest("/api/auth/login", {
        method: "POST",
        body: { email, password },
      });
      saveAuth({ token: result.accessToken, user: result.user });
      return result.user;
    },
    [saveAuth]
  );

  const register = useCallback(
    async (form) => {
      await apiRequest("/api/auth/register", { method: "POST", body: form });
      return login(form.email, form.password); // log straight in after signing up
    },
    [login]
  );

  const value = useMemo(
    () => ({
      user: auth.user,
      token: auth.token,
      isLoggedIn: Boolean(auth.token),
      isAdmin: auth.user?.role === "ADMIN",
      checking,
      login,
      register,
      logout,
    }),
    [auth, checking, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}