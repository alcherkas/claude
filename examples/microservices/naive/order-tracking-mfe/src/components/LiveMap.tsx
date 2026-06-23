import { useMemo } from 'react';
import type { TrackingPoint } from '../types';

interface LiveMapProps {
  points: TrackingPoint[];
}

const WIDTH = 480;
const HEIGHT = 280;
const PAD = 24;

// Simple placeholder map: projects the courier's tracking points into the SVG
// viewport (normalized by their own bounding box) and draws the path plus the
// latest position. A production build would swap this for a real tile map.
export default function LiveMap({ points }: LiveMapProps) {
  const projected = useMemo(() => {
    if (points.length === 0) {
      return [];
    }
    const lats = points.map((p) => p.lat);
    const lngs = points.map((p) => p.lng);
    const minLat = Math.min(...lats);
    const maxLat = Math.max(...lats);
    const minLng = Math.min(...lngs);
    const maxLng = Math.max(...lngs);
    const spanLat = maxLat - minLat || 1;
    const spanLng = maxLng - minLng || 1;

    return points.map((p) => ({
      // lng -> x, lat -> y (inverted so north is up).
      x: PAD + ((p.lng - minLng) / spanLng) * (WIDTH - 2 * PAD),
      y: PAD + (1 - (p.lat - minLat) / spanLat) * (HEIGHT - 2 * PAD),
    }));
  }, [points]);

  if (projected.length === 0) {
    return <div className="ot-map ot-map--empty">No tracking points yet.</div>;
  }

  const path = projected.map((pt, i) => `${i === 0 ? 'M' : 'L'} ${pt.x} ${pt.y}`).join(' ');
  const last = projected[projected.length - 1];

  return (
    <svg
      className="ot-map"
      viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
      role="img"
      aria-label="Live courier tracking map"
    >
      <rect x="0" y="0" width={WIDTH} height={HEIGHT} className="ot-map__bg" />
      <path d={path} className="ot-map__route" fill="none" />
      {projected.map((pt, i) => (
        <circle key={i} cx={pt.x} cy={pt.y} r={3} className="ot-map__point" />
      ))}
      <circle cx={last.x} cy={last.y} r={7} className="ot-map__courier" />
    </svg>
  );
}
