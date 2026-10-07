import { useState } from "react";
import ManifestPanel from "../components/admin/ManifestPanel.jsx";
import VerificationPanel from "../components/admin/VerificationPanel.jsx";

const TABS = [
  { id: "verify", label: "Verify divers" },
  { id: "manifest", label: "Passenger manifests" },
];

export default function AdminPage() {
  const [tab, setTab] = useState("verify");

  return (
    <section>
      <h1>Admin</h1>
      <p className="muted no-print">
        Verify divers' certification cards and check who's on each boat.
      </p>

      <div className="tabs no-print" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.id}
            type="button"
            role="tab"
            aria-selected={tab === t.id}
            className={`tab ${tab === t.id ? "tab-active" : ""}`}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === "verify" ? <VerificationPanel /> : <ManifestPanel />}
    </section>
  );
}