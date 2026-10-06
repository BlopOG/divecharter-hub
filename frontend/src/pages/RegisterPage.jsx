import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { useAuth } from "../context/auth-context.js";

// Must match the backend's CertificationLevel enum
const CERT_LEVELS = [
  { value: "OPEN_WATER", label: "Open Water (up to 18 m)" },
  { value: "ADVANCED_OPEN_WATER", label: "Advanced Open Water (up to 30 m)" },
  { value: "RESCUE_DIVER", label: "Rescue Diver (up to 30 m)" },
  { value: "DEEP_SPECIALTY", label: "Deep Specialty (up to 40 m)" },
  { value: "DIVEMASTER", label: "Divemaster (up to 40 m)" },
];

const EMPTY_FORM = {
  fullName: "",
  email: "",
  password: "",
  certLevel: "OPEN_WATER",
  certAgency: "",
  certNumber: "",
};

function FieldError({ message }) {
  return message ? <span className="field-error">{message}</span> : null;
}

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (event) => setForm((current) => ({ ...current, [field]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setSubmitting(true);
    try {
      await register(form);
      navigate("/trips", { replace: true });
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors ?? {}); // per-field messages from the backend's validation
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="form-card">
      <h1>Create your account</h1>
      <p className="muted">Sign up to book dives that match your certification.</p>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          Full name
          <input
            autoComplete="name"
            required
            value={form.fullName}
            onChange={update("fullName")}
            aria-invalid={Boolean(fieldErrors.fullName)}
          />
          <FieldError message={fieldErrors.fullName} />
        </label>

        <label className="field">
          Email
          <input
            type="email"
            autoComplete="email"
            required
            value={form.email}
            onChange={update("email")}
            aria-invalid={Boolean(fieldErrors.email)}
          />
          <FieldError message={fieldErrors.email} />
        </label>

        <label className="field">
          Password
          <input
            type="password"
            autoComplete="new-password"
            required
            value={form.password}
            onChange={update("password")}
            aria-invalid={Boolean(fieldErrors.password)}
          />
          <span className="muted small">8–72 characters, with at least one letter and one number.</span>
          <FieldError message={fieldErrors.password} />
        </label>

        <label className="field">
          Certification level
          <select value={form.certLevel} onChange={update("certLevel")}>
            {CERT_LEVELS.map((level) => (
              <option key={level.value} value={level.value}>
                {level.label}
              </option>
            ))}
          </select>
          <FieldError message={fieldErrors.certLevel} />
        </label>

        <div className="field-row">
          <label className="field">
            <span>
              Agency <span className="optional">(optional)</span>
            </span>
            <input placeholder="PADI, SSI…" value={form.certAgency} onChange={update("certAgency")} />
            <FieldError message={fieldErrors.certAgency} />
          </label>
          <label className="field">
            <span>
              Card number <span className="optional">(optional)</span>
            </span>
            <input value={form.certNumber} onChange={update("certNumber")} />
            <FieldError message={fieldErrors.certNumber} />
          </label>
        </div>

        <p className="notice">
          After you sign up, a staff member verifies your certification card. You can browse right
          away, and booking unlocks once you're verified.
        </p>

        <button type="submit" disabled={submitting}>
          {submitting ? "Creating account…" : "Create account"}
        </button>
      </form>

      <p className="muted">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </section>
  );
}