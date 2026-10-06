import { Link } from "react-router";

export default function NotFoundPage() {
  return (
    <section className="hero">
      <h1>Page not found</h1>
      <p className="muted">That page doesn't exist.</p>
      <Link to="/" className="button">
        Go home
      </Link>
    </section>
  );
}