import { Link } from "react-router";

export default function HomePage() {
  return (
    <section className="hero">
      <h1>Book your next dive, safely.</h1>
      <p>
        Browse upcoming boat trips to the world's best dive sites. We check every booking against
        your certification, so you only dive within your training.
      </p>
      <Link to="/trips" className="button button-large">
        Browse trips
      </Link>

      <div className="features">
        <div>
          <h3>Certification-checked</h3>
          <p className="muted">Sites deeper than your certification allows can't be booked.</p>
        </div>
        <div>
          <h3>Never overbooked</h3>
          <p className="muted">Live seat counts, and the boat can't sell the same seat twice.</p>
        </div>
        <div>
          <h3>Flexible</h3>
          <p className="muted">Cancel online up to 24 hours before departure.</p>
        </div>
      </div>
    </section>
  );
}