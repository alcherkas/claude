import type { Restaurant } from '../types';
import { RestaurantCard } from './RestaurantCard';

interface RestaurantListProps {
  restaurants: Restaurant[];
  isLoading: boolean;
  isError: boolean;
  emptyLabel?: string;
}

// Grid of restaurant cards with loading / error / empty states.
export function RestaurantList({
  restaurants,
  isLoading,
  isError,
  emptyLabel = 'No restaurants match your search.',
}: RestaurantListProps) {
  if (isLoading) {
    return <div className="dc-status">Loading restaurants…</div>;
  }
  if (isError) {
    return (
      <div className="dc-status dc-status--error">
        Could not load restaurants. Please try again.
      </div>
    );
  }
  if (restaurants.length === 0) {
    return <div className="dc-status">{emptyLabel}</div>;
  }

  return (
    <div className="dc-grid">
      {restaurants.map((r) => (
        <RestaurantCard key={r.id} restaurant={r} />
      ))}
    </div>
  );
}
