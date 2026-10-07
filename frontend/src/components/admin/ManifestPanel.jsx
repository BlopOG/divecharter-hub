
import { useEffect, useState } from "react";
import { apiRequest } from "../../api/client.js";
import { useAuth } from "../../context/auth-context.js";
import { certificationLabel, formatDateTime } from "../../utils/format.js";

export default function ManifestPanel() {
  const { token } = useAuth();
  const [trips, setTrips] = useState(null);
  const [tripId, setTripId] = useState("");
  const [manifest, setManifest] = useState(null);
  const [error, setError] = useState("");

  // Load upcoming trips for the dropdown (public endpoint)
  useEffect(() => {
    let cancelled = false;

    apiRequest("/api/trips?size=50&sort=departureTime")
      .then((page) => {
        if (!cancelled) setTrips(page.content);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // Load the manifest whenever a trip is chosen (admin-only endpoint)
  useEffect(() => {
    if (!tripId) return undefined;
    let cancelled = false;

    apiRequest(`/api/admin/trips/${tripId}/manifest`, { token })
      .then((result) => {
        if (!cancelled) setManifest(result);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, [tripId, token]);

  function handleTripChange(event) {
    setManifest(null);
    setError("");
    setTripId(event.target.value);
  }

  return (
    <div>
      <div className="toolbar no-print">
        <label className="inline-field">
          Trip
          <select value={tripId} onChange={handleTripChange} disabled={!trips}>
            <option value="">{trips ? "Choose a trip…" : "Loading trips…"}</option>
            {trips?.map((trip) => (
              <option key={trip.id} value={trip.id}>
                {formatDateTime(trip.departureTime)}: {trip.siteName} ({trip.boatName}) ·{" "}
                {trip.seatsBooked}/{trip.capacity} booked
              </option>
            ))}
          </select>
        </label>
        {manifest && (
          <button type="button" className="button-secondary-neutral" onClick={() => window.print()}>
            Print manifest
          </button>
        )}
      </div>

      {error && <p className="error">{error}</p>}
      {tripId && !manifest && !error && <p>Loading manifest…</p>}
      {!tripId && !error && <p className="muted">Choose a trip to see who's on board.</p>}

      {manifest && (
        <div className="manifest">
          <div className="manifest-header">
            <div>
              <h2>{manifest.siteName}</h2>
              <p className="muted">
                {manifest.boatName} · departs {formatDateTime(manifest.departureTime)} · returns{" "}
                {formatDateTime(manifest.returnTime)}
              </p>
            </div>
            <span className="badge badge-verified">
              {manifest.seatsBooked} / {manifest.capacity} seats booked
            </span>
          </div>

          {manifest.passengers.length === 0 ? (
            <div className="empty-state">
              <p>No passengers booked on this trip yet.</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Diver</th>
                    <th>Certification</th>
                    <th>Card</th>
                  </tr>
                </thead>
                <tbody>
                  {manifest.passengers.map((p, index) => (
                    <tr key={p.bookingId}>
                      <td>{index + 1}</td>
                      <td>
                        <strong>{p.fullName}</strong>
                        <div className="muted small">{p.email}</div>
                      </td>
                      <td>{certificationLabel(p.certLevel)}</td>
                      <td>
                        {p.certAgency ?? "—"}
                        {p.certNumber && <div className="muted small">#{p.certNumber}</div>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}