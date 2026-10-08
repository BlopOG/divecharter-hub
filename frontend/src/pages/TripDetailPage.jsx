import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { apiRequest } from "../api/client.js";
import SiteImage from "../components/SiteImage.jsx"; // NEW
import { useAuth } from "../context/auth-context.js";
import { certificationLabel, depthZone, formatDateTime, formatPrice } from "../utils/format.js";

// Works out, before booking, whether this user can book this trip (and why not)
function getBookingBlock(trip, user, isLoggedIn) {
  if (trip.seatsAvailable <= 0) return "This trip is fully booked.";
  if (!isLoggedIn || !user) return null;
  if (!user.certVerified) {
    return "Your certification hasn't been verified yet. A staff member needs to check your card before you can book.";
  }
  if (trip.maxDepthMeters > user.maxDepthMeters) {
    return `This site reaches ${trip.maxDepthMeters} m, but your ${certificationLabel(user.certLevel)} certification covers dives to ${user.maxDepthMeters} m.`;
  }
  return null;
}

export default function TripDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, token, isLoggedIn } = useAuth();

  // Remember which trip id the loaded data belongs to
  const [loaded, setLoaded] = useState({ id: null, trip: null, error: "" });
  const [booking, setBooking] = useState(null);
  const [bookingError, setBookingError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;

    apiRequest(`/api/trips/${id}`)
      .then((trip) => {
        if (!cancelled) setLoaded({ id, trip, error: "" });
      })
      .catch((err) => {
        if (!cancelled) setLoaded({ id, trip: null, error: err.message });
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Only use the data if it's for the trip in the current URL
  const isCurrent = loaded.id === id;
  const trip = isCurrent ? loaded.trip : null;
  const error = isCurrent ? loaded.error : "";

  async function handleBook() {
    if (!isLoggedIn) {
      navigate("/login", { state: { from: `/trips/${id}` } });
      return;
    }
    setBookingError("");
    setSubmitting(true);
    try {
      const created = await apiRequest("/api/bookings", {
        method: "POST",
        body: { tripId: trip.id },
        token,
      });
      setBooking(created);
      // Show the new seat count straight away
      setLoaded((current) => ({
        ...current,
        trip: {
          ...current.trip,
          seatsBooked: current.trip.seatsBooked + 1,
          seatsAvailable: current.trip.seatsAvailable - 1,
        },
      }));
    } catch (err) {
      setBookingError(err.message); // the backend's own explanation (409, 422, …)
    } finally {
      setSubmitting(false);
    }
  }

  if (error) {
    return (
      <section>
        <p className="error">{error}</p>
        <Link to="/trips">← Back to trips</Link>
      </section>
    );
  }
  if (!trip) return <p>Loading trip…</p>;

  const blockReason = getBookingBlock(trip, user, isLoggedIn);
  const canDive = isLoggedIn && user && !blockReason;

  return (
    <section className={`detail depth-${depthZone(trip.maxDepthMeters)}`}>
      <Link to="/trips">← Back to trips</Link>

      {/* NEW: large site photo */}
      <SiteImage
        src={trip.siteImageUrl}
        alt={`Diving at ${trip.siteName}`}
        className="detail-image"
      />

      <div className="card-header">
        <h1>{trip.siteName}</h1>
        <span className="depth-badge">{trip.maxDepthMeters} m</span>
      </div>
      <p className="muted">{trip.siteLocation}</p>

      <dl className="detail-grid">
        <dt>Departs</dt>
        <dd>{formatDateTime(trip.departureTime)}</dd>
        <dt>Returns</dt>
        <dd>{formatDateTime(trip.returnTime)}</dd>
        <dt>Boat</dt>
        <dd>{trip.boatName}</dd>
        <dt>Difficulty</dt>
        <dd>{trip.difficulty.toLowerCase()}</dd>
        <dt>Seats left</dt>
        <dd>
          {trip.seatsAvailable} of {trip.capacity}
        </dd>
        <dt>Price</dt>
        <dd>{formatPrice(trip.price)}</dd>
      </dl>

      <div className="booking-panel">
        {booking ? (
          <div className="success" role="status">
            <strong>You're booked!</strong> See you on {trip.boatName} on{" "}
            {formatDateTime(booking.departureTime)}.{" "}
            <Link to="/my-bookings">View my bookings</Link>
          </div>
        ) : (
          <>
            {canDive && (
              <p className="eligible">
                ✓ Your {certificationLabel(user.certLevel)} certification covers this{" "}
                {trip.maxDepthMeters} m dive.
              </p>
            )}
            {blockReason && <p className="notice notice-warning">{blockReason}</p>}
            {bookingError && (
              <p className="error" role="alert">
                {bookingError}
              </p>
            )}
            <button
              type="button"
              className="button-large"
              onClick={handleBook}
              disabled={submitting || Boolean(blockReason)}
            >
              {!isLoggedIn
                ? "Log in to book"
                : submitting
                  ? "Booking…"
                  : `Book for ${formatPrice(trip.price)}`}
            </button>
          </>
        )}
      </div>
    </section>
  );
}