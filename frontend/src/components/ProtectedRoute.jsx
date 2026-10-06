import { Navigate, useLocation } from "react-router";
import { useAuth } from "../context/auth-context.js";

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { isLoggedIn, isAdmin, checking } = useAuth();
  const location = useLocation();

  if (checking) {
    return <p>Checking your session…</p>;
  }

  if (!isLoggedIn) {
    // Remember where they were going, so login can send them back there
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (adminOnly && !isAdmin) {
    return (
      <section>
        <h1>Admins only</h1>
        <p className="muted">You don't have permission to view this page.</p>
      </section>
    );
  }

  return children;
}