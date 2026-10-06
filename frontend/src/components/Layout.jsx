import { NavLink, Outlet } from "react-router";

export default function Layout() {
  return (
    <>
      <header className="navbar">
        <NavLink to="/" className="brand">
         DiveMatrix
        </NavLink>
        <nav>
          <NavLink to="/sites">Dive sites</NavLink>
          <NavLink to="/trips">Trips</NavLink>
          <NavLink to="/my-bookings">My bookings</NavLink>
          <NavLink to="/admin">Admin</NavLink>
          <NavLink to="/login">Log in</NavLink>
          <NavLink to="/register" className="button">
            Sign up
          </NavLink>
        </nav>
      </header>

      <main className="container">
        <Outlet />
      </main>

      <footer className="footer">DiveMatrix · UCI 2123 Capstone</footer>
    </>
  );
}