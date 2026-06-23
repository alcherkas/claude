import { Link } from 'react-router-dom';
import type { Restaurant } from '../types';

interface RestaurantCardProps {
  restaurant: Restaurant;
}

// Compact restaurant tile linking through to its detail + menu view.
export function RestaurantCard({ restaurant }: RestaurantCardProps) {
  const closed = restaurant.status !== 'ACTIVE';
  return (
    <Link
      to={`restaurants/${restaurant.id}`}
      className={`dc-card${closed ? ' dc-card--closed' : ''}`}
    >
      <div className="dc-card__body">
        <h3 className="dc-card__name">{restaurant.name}</h3>
        <p className="dc-card__cuisine">{restaurant.cuisine}</p>
        <p className="dc-card__address">{restaurant.address}</p>
      </div>
      <div className="dc-card__meta">
        <span className="dc-card__hours">{restaurant.openingHours}</span>
        <span
          className={`dc-badge dc-badge--${restaurant.status.toLowerCase()}`}
        >
          {restaurant.status}
        </span>
      </div>
    </Link>
  );
}
