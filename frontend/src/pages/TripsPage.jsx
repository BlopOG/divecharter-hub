import { useEffect, useState } from "react";
import { Link } from "react-router";
import { apiRequest } from "../api/client.js";
import { depthZone, formatDateTime, formatPrice } from "../utils/format.js";

const PAGE_SIZE = 6;

export default function TripsPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    apiRequest(`/api/trips?page=${page}&size=${PAGE_SIZE}&sort=departureTime`)
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    // If the user changes page quickly, ignore the older, slower response
    return () => {
      cancelled = true;
    };
  }, [page]);

  // Reset loading/error here (in the click handler), not inside the effect
  function goToPage(nextPage) {
    setLoading(true);
    setError("");
    setPage(nextPage);
  }

  if (loading && !data) return <p>Loading trips…</p>;
  if (error) return <p className="error">{error}</p>;

  return (
    <section>
      <h1>Upcoming trips</h1>

      {data.content.length === 0 ? (
        <p className="muted">No upcoming trips right now.</p>
      ) : (
        <div className="grid">
          {data.content.map((trip) => (
            <article key={trip.id} className={`card depth-${depthZone(trip.maxDepthMeters)}`}>
              <div className="card-header">
                <h2>{trip.siteName}</h2>
                <span className="depth-badge">{trip.maxDepthMeters} m</span>
              </div>
              <p className="muted">{trip.siteLocation}</p>
              <ul className="facts">
                <li>{formatDateTime(trip.departureTime)}</li>
                <li>Boat: {trip.boatName}</li>
                <li>Difficulty: {trip.difficulty.toLowerCase()}</li>
                <li>
                  {trip.seatsAvailable} of {trip.capacity} seats left
                </li>
              </ul>
              <div className="card-footer">
                <strong>{formatPrice(trip.price)}</strong>
                <Link to={`/trips/${trip.id}`} className="button">
                  View trip
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}

      <div className="pagination">
        <button onClick={() => goToPage(page - 1)} disabled={page === 0}>
          ← Previous
        </button>
        <span>
          Page {data.page + 1} of {Math.max(data.totalPages, 1)}
        </span>
        <button onClick={() => goToPage(page + 1)} disabled={data.last}>
          Next →
        </button>
      </div>
    </section>
  );
}