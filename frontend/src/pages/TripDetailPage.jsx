import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { apiRequest } from "../api/client.js";
import { formatDateTime, formatPrice } from "../utils/format.js";

export default function TripDetailPage() {
  const { id } = useParams();
  const [trip, setTrip] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setTrip(null);
    setError("");

    apiRequest(`/api/trips/${id}`)
      .then((result) => {
        if (!cancelled) setTrip(result);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (error) {
    return (
      <section>
        <p className="error">{error}</p>
        <Link to="/trips">← Back to trips</Link>
      </section>
    );
  }
  if (!trip) return <p>Loading trip…</p>;

  return (
    <section className="detail">
      <Link to="/trips">← Back to trips</Link>
      <h1>{trip.siteName}</h1>
      <p className="muted">{trip.siteLocation}</p>

      <dl className="detail-grid">
        <dt>Departs</dt>
        <dd>{formatDateTime(trip.departureTime)}</dd>
        <dt>Returns</dt>
        <dd>{formatDateTime(trip.returnTime)}</dd>
        <dt>Boat</dt>
        <dd>{trip.boatName}</dd>
        <dt>Max depth</dt>
        <dd>{trip.maxDepthMeters} m</dd>
        <dt>Difficulty</dt>
        <dd>{trip.difficulty.toLowerCase()}</dd>
        <dt>Seats left</dt>
        <dd>
          {trip.seatsAvailable} of {trip.capacity}
        </dd>
        <dt>Price</dt>
        <dd>{formatPrice(trip.price)}</dd>
      </dl>

      <button disabled title="Booking is added in a later step">
        Book this trip
      </button>
    </section>
  );
}