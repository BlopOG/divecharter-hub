import { useState } from "react";

// Shows a dive site photo, or a depth-colored ocean gradient if there's no photo
// or the file can't be loaded, so a card never shows a broken image.
export default function SiteImage({ src, alt, className = "card-image" }) {
  const [failed, setFailed] = useState(false);

  if (!src || failed) {
    return <div className={`${className} image-fallback`} aria-hidden="true" />;
  }

  return (
    <img
      className={className}
      src={src}
      alt={alt}
      loading="lazy"
      onError={() => setFailed(true)}
    />
  );
}