import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getRestaurant } from '../api/client';
import { MenuView } from './MenuView';

// Restaurant detail page: header info + embedded MenuView for the menu. Route
// param :restaurantId is supplied by App's routes.
export function RestaurantDetail() {
  const { restaurantId } = useParams<{ restaurantId: string }>();

  const restaurantQuery = useQuery({
    queryKey: ['restaurant', restaurantId],
    queryFn: () => getRestaurant(restaurantId as string),
    enabled: Boolean(restaurantId),
  });

  if (!restaurantId) {
    return <div className="dc-status dc-status--error">Unknown restaurant.</div>;
  }
  if (restaurantQuery.isLoading) {
    return <div className="dc-status">Loading restaurant…</div>;
  }
  if (restaurantQuery.isError || !restaurantQuery.data) {
    return (
      <div className="dc-status dc-status--error">
        Could not load this restaurant.
      </div>
    );
  }

  const restaurant = restaurantQuery.data;

  return (
    <article className="dc-detail">
      <Link to=".." relative="path" className="dc-detail__back">
        ← Back to results
      </Link>

      <header className="dc-detail__header">
        <h1 className="dc-detail__name">{restaurant.name}</h1>
        <p className="dc-detail__cuisine">{restaurant.cuisine}</p>
        <p className="dc-detail__address">{restaurant.address}</p>
        <p className="dc-detail__hours">Open: {restaurant.openingHours}</p>
        <span
          className={`dc-badge dc-badge--${restaurant.status.toLowerCase()}`}
        >
          {restaurant.status}
        </span>
      </header>

      <h2 className="dc-detail__menu-title">Menu</h2>
      <MenuView restaurantId={restaurant.id} />
    </article>
  );
}
