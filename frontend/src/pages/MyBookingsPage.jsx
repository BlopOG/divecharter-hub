import { useEffect, useState } from "react";
import { Link } from "react-router";
import { apiRequest } from "../api/client.js";
import { useAuth } from "../context/auth-context.js";
import { formatDateTime, formatPrice } from "../utils/format.js";

const PAGE_SIZE = 10;
const CUTOFF_MS = 24 * 60 * 60 * 1000; // matches the backend's 24-hour cancellation rule

function bookingState(booking, now, isAdmin) {
  const departure = new Date(booking.departureTime).getTime();
  const isPast = departure <= now;
  const insideCutoff = departure - now < CUTOFF_MS;

  const label =
    booking.status === "CANCELLED" ? "Cancelled" : isPast ? "Completed" : "Confirmed";
  const canCancel = booking.status === "CONFIRMED" && !isPast && (isAdmin || !insideCutoff);
  const cutoffPassed = booking.status === "CONFIRMED" && !isPast && !canCancel;

  return { label, canCancel, cutoffPassed };
}

export default function MyBookingsPage() {
  const { token, isAdmin } = useAuth();
  const [now] = useState(() => Date.now());
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    let cancelled = false;

    apiRequest(`/api/bookings/me?page=${page}&size=${PAGE_SIZE}`, { token })
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, [page, token]);

  async function handleCancel(booking) {
    if (!window.confirm(`Cancel your booking for ${booking.siteName}?`)) return;

    setActionError("");
    setCancellingId(booking.id);
    try {
      const updated = await apiRequest(`/api/bookings/${booking.id}/cancel`, {
        method: "PATCH",
        token,
      });
      // Swap the cancelled booking into the list without reloading
      setData((current) => ({
        ...current,
        content: current.content.map((b) => (b.id === updated.id ? updated : b)),
      }));
    } catch (err) {
      setActionError(err.message);
    } finally {
      setCancellingId(null);
    }
  }

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p>Loading your bookings…</p>;

  return (
    <section>
      <h1>My bookings</h1>

      {actionError && (
        <p className="error" role="alert">
          {actionError}
        </p>
      )}

      {data.content.length === 0 ? (
        <div className="empty-state">
          <p>You haven't booked any dives yet.</p>
          <Link to="/trips" className="button">
            Find a trip
          </Link>
        </div>
      ) : (
        <ul className="booking-list">
          {data.content.map((booking) => {
            const { label, canCancel, cutoffPassed } = bookingState(booking, now, isAdmin);
            return (
              <li key={booking.id} className="booking-row">
                <div>
                  <h2>
                    <Link to={`/trips/${booking.tripId}`}>{booking.siteName}</Link>
                  </h2>
                  <p className="muted">
                    {formatDateTime(booking.departureTime)} · {booking.boatName}
                  </p>
                </div>

                <div className="booking-meta">
                  <span className={`status status-${label.toLowerCase()}`}>{label}</span>
                  <strong>{formatPrice(booking.totalPrice)}</strong>
                  {canCancel && (
                    <button
                      type="button"
                      className="button-secondary"
                      onClick={() => handleCancel(booking)}
                      disabled={cancellingId === booking.id}
                    >
                      {cancellingId === booking.id ? "Cancelling…" : "Cancel"}
                    </button>
                  )}
                  {cutoffPassed && (
                    <span className="muted small">Cancellation closes 24 h before departure</span>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}

      {data.totalPages > 1 && (
        <div className="pagination">
          <button onClick={() => setPage((p) => p - 1)} disabled={page === 0}>
            ← Previous
          </button>
          <span>
            Page {data.page + 1} of {data.totalPages}
          </span>
          <button onClick={() => setPage((p) => p + 1)} disabled={data.last}>
            Next →
          </button>
        </div>
      )}
    </section>
  );
}