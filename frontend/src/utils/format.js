// Turns "2026-10-14T13:00:00" into "Wed, Oct 14, 1:00 PM"
export function formatDateTime(value) {
  return new Date(value).toLocaleString(undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

// Turns 110 into "$110.00"
export function formatPrice(value) {
  return new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" }).format(value);
}

// Matches certification limits: Open Water 18 m, Advanced 30 m, Deep/Divemaster 40 m
export function depthZone(depthMeters) {
  if (depthMeters <= 18) return "shallow";
  if (depthMeters <= 30) return "mid";
  return "deep";
}
export function requiredCertification(depthMeters) {
  if (depthMeters <= 18) return "Open Water";
  if (depthMeters <= 30) return "Advanced Open Water";
  return "Deep Specialty";
}