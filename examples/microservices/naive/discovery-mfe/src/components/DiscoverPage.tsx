import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { listRestaurants, search } from '../api/client';
import type { Restaurant, SearchResponse } from '../types';
import { SearchBar, type SearchCriteria } from './SearchBar';
import { RestaurantList } from './RestaurantList';

const CUISINES = [
  'Italian',
  'Japanese',
  'Mexican',
  'Indian',
  'Chinese',
  'Thai',
  'American',
  'Mediterranean',
];

// Landing page of the discovery MFE. Shows the full restaurant listing by
// default (GET /api/restaurants) and switches to search results
// (GET /api/search) once the user submits a query. Both feed RestaurantList.
export function DiscoverPage() {
  const [criteria, setCriteria] = useState<SearchCriteria>({
    q: '',
    cuisine: '',
  });

  const hasQuery = criteria.q.length > 0;

  // Default browse: list restaurants, optionally filtered by cuisine.
  const listQuery = useQuery({
    queryKey: ['restaurants', criteria.cuisine],
    queryFn: () => listRestaurants(criteria.cuisine || undefined),
    enabled: !hasQuery,
  });

  // Active search: hit the search index and project restaurant hits.
  const searchQuery = useQuery<SearchResponse>({
    queryKey: ['search', criteria.q, criteria.cuisine],
    queryFn: () =>
      search({
        q: criteria.q,
        cuisine: criteria.cuisine || undefined,
      }),
    enabled: hasQuery,
  });

  // Search hits are denormalized; collapse them to unique restaurants so the
  // same RestaurantList renders both browse and search modes.
  const searchResults: Restaurant[] = useMemo(() => {
    const hits = searchQuery.data?.hits ?? [];
    const byRestaurant = new Map<string, Restaurant>();
    for (const hit of hits) {
      if (byRestaurant.has(hit.restaurantId)) continue;
      byRestaurant.set(hit.restaurantId, {
        id: hit.restaurantId,
        ownerUserId: '',
        name: hit.name,
        cuisine: hit.cuisine,
        address: '',
        geo: hit.geo ?? { lat: 0, lng: 0 },
        status: hit.available ? 'ACTIVE' : 'SUSPENDED',
        openingHours: '',
      });
    }
    return Array.from(byRestaurant.values());
  }, [searchQuery.data]);

  const active = hasQuery ? searchQuery : listQuery;
  const restaurants = hasQuery ? searchResults : (listQuery.data ?? []);

  return (
    <div className="dc-discover">
      <SearchBar
        initial={criteria}
        cuisines={CUISINES}
        onSearch={setCriteria}
      />

      <h2 className="dc-discover__heading">
        {hasQuery
          ? `Results for “${criteria.q}”`
          : criteria.cuisine
            ? `${criteria.cuisine} restaurants`
            : 'Browse restaurants'}
      </h2>

      <RestaurantList
        restaurants={restaurants}
        isLoading={active.isLoading}
        isError={active.isError}
        emptyLabel={
          hasQuery
            ? 'No restaurants matched your search.'
            : 'No restaurants available right now.'
        }
      />
    </div>
  );
}
