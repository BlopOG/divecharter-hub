import { createContext, useContext } from "react";

export const AuthContext = createContext(null);

/** Use anywhere: const { user, isLoggedIn, isAdmin, login, logout } = useAuth(); */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside <AuthProvider>");
  }
  return context;
}