import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../api/client.js";
import { depthZone, requiredCertification } from "../utils/format.js";

export default function SitesPage() {
  const [sites, setSites] = useState([]);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    apiRequest("/api/dive-sites")
      .then((result) => {
        if (!cancelled) setSites(result);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // Filter by the search box, then sort shallowest to deepest
  const visibleSites = useMemo(() => {
    const q = query.trim().toLowerCase();
    const filtered = q
      ? sites.filter(
          (site) => site.name.toLowerCase().includes(q) || site.location.toLowerCase().includes(q)
        )
      : sites;
    return [...filtered].sort((a, b) => a.maxDepthMeters - b.maxDepthMeters);
  }, [sites, query]);

  if (loading) return <p>Loading dive sites…</p>;
  if (error) return <p className="error">{error}</p>;

  return (
    <section>
      <h1>Dive sites</h1>
      <p className="muted">
        Every site we visit, from shallow reefs to deep walls. The color shows which certification
        you need.
      </p>

      <input
        type="search"
        className="search"
        placeholder="Search by name or location…"
        aria-label="Search dive sites"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      <ul className="legend">
        <li className="depth-shallow">Up to 18 m · Open Water</li>
        <li className="depth-mid">Up to 30 m · Advanced Open Water</li>
        <li className="depth-deep">Up to 40 m · Deep Specialty</li>
      </ul>

      {visibleSites.length === 0 ? (
        <p className="muted">No sites match "{query}".</p>
      ) : (
        <div className="grid">
          {visibleSites.map((site) => (
            <article key={site.id} className={`card depth-${depthZone(site.maxDepthMeters)}`}>
              <div className="card-header">
                <h2>{site.name}</h2>
                <span className="depth-badge">{site.maxDepthMeters} m</span>
              </div>
              <p className="muted">{site.location}</p>
              {site.description && <p className="site-description">{site.description}</p>}
              <ul className="facts">
                <li>Difficulty: {site.difficulty.toLowerCase()}</li>
                <li>Requires: {requiredCertification(site.maxDepthMeters)}</li>
              </ul>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}