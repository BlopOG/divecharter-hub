import { useEffect, useState } from "react";
import { apiRequest } from "../../api/client.js";
import { useAuth } from "../../context/auth-context.js";
import { certificationLabel } from "../../utils/format.js";

export default function VerificationPanel() {
  const { token } = useAuth();
  const [pendingOnly, setPendingOnly] = useState(true);
  const [divers, setDivers] = useState(null);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [busyId, setBusyId] = useState(null);

  useEffect(() => {
    let cancelled = false;

    apiRequest(`/api/admin/users?pendingOnly=${pendingOnly}&size=50`, { token })
      .then((page) => {
        if (!cancelled) setDivers(page.content);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, [pendingOnly, token]);

  function handleFilterChange(event) {
    setDivers(null); // show "Loading…" while the new list arrives
    setError("");
    setPendingOnly(event.target.value === "pending");
  }

  async function setVerified(diver, verified) {
    if (!verified && !window.confirm(`Revoke ${diver.fullName}'s verification? They won't be able to book.`)) {
      return;
    }
    setActionError("");
    setBusyId(diver.id);
    try {
      const updated = await apiRequest(`/api/admin/users/${diver.id}/certification`, {
        method: "PATCH",
        body: { verified },
        token,
      });
      setDivers((current) =>
        pendingOnly && updated.certVerified
          ? current.filter((d) => d.id !== updated.id) // verified → leaves the "waiting" list
          : current.map((d) => (d.id === updated.id ? updated : d))
      );
    } catch (err) {
      setActionError(err.message);
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      <div className="toolbar">
        <label className="inline-field">
          Show
          <select value={pendingOnly ? "pending" : "all"} onChange={handleFilterChange}>
            <option value="pending">Waiting for verification</option>
            <option value="all">All divers</option>
          </select>
        </label>
        {divers && (
          <span className="muted">
            {divers.length} diver{divers.length === 1 ? "" : "s"}
          </span>
        )}
      </div>

      {error && <p className="error">{error}</p>}
      {actionError && (
        <p className="error" role="alert">
          {actionError}
        </p>
      )}
      {!divers && !error && <p>Loading divers…</p>}

      {divers && divers.length === 0 && (
        <div className="empty-state">
          <p>
            {pendingOnly
              ? "All caught up. No certifications are waiting to be checked."
              : "No divers have registered yet."}
          </p>
        </div>
      )}

      {divers && divers.length > 0 && (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Diver</th>
                <th>Certification</th>
                <th>Card</th>
                <th>Status</th>
                <th>
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {divers.map((diver) => (
                <tr key={diver.id}>
                  <td>
                    <strong>{diver.fullName}</strong>
                    <div className="muted small">{diver.email}</div>
                  </td>
                  <td>
                    {certificationLabel(diver.certLevel)}
                    <div className="muted small">up to {diver.maxDepthMeters} m</div>
                  </td>
                  <td>
                    {diver.certAgency ?? "—"}
                    {diver.certNumber && <div className="muted small">#{diver.certNumber}</div>}
                  </td>
                  <td>
                    <span className={`badge ${diver.certVerified ? "badge-verified" : "badge-pending"}`}>
                      {diver.certVerified ? "Verified" : "Pending"}
                    </span>
                  </td>
                  <td className="actions">
                    {diver.certVerified ? (
                      <button
                        type="button"
                        className="button-secondary"
                        onClick={() => setVerified(diver, false)}
                        disabled={busyId === diver.id}
                      >
                        Revoke
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={() => setVerified(diver, true)}
                        disabled={busyId === diver.id}
                      >
                        {busyId === diver.id ? "Verifying…" : "Verify"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}