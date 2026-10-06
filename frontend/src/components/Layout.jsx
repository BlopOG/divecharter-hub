import { NavLink, Outlet, useNavigate } from "react-router";
import { useAuth } from "../context/auth-context.js";

export default function Layout() {
  const { user, isLoggedIn, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/");
  }

  return (
    <>
      <header className="navbar">
        <NavLink to="/" className="brand">
          DiveMatrix
        </NavLink>
        <nav>
          <NavLink to="/sites">Dive sites</NavLink>
          <NavLink to="/trips">Trips</NavLink>
          {isLoggedIn && <NavLink to="/my-bookings">My bookings</NavLink>}
          {isAdmin && <NavLink to="/admin">Admin</NavLink>}

          {isLoggedIn ? (
            <>
              <span className="user-chip" title={user?.email}>
                {user?.fullName}
                {user && !user.certVerified && <span className="pending-tag">unverified</span>}
              </span>
              <button type="button" className="link-button" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login">Log in</NavLink>
              <NavLink to="/register" className="button">
                Sign up
              </NavLink>
            </>
          )}
        </nav>
      </header>

      <main className="container">
        <Outlet />
      </main>

      <footer className="footer">DiveMatrix · UCI 2123 Capstone</footer>
    </>
  );
}