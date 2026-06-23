import { useEffect, useState } from 'react';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { createRestaurant, myRestaurants, setStatus } from '../api/client';
import type {
  CreateRestaurantRequest,
  Restaurant,
  RestaurantStatus,
} from '../types';
import { useSelectedRestaurant } from '../lib/selectedRestaurant';

const STATUS_BADGE: Record<RestaurantStatus, string> = {
  ACTIVE: 'ra-badge--ok',
  PENDING: 'ra-badge--warn',
  SUSPENDED: 'ra-badge--danger',
};

// Owners can move ACTIVE <-> SUSPENDED themselves; PENDING is set by the
// platform on creation and cleared by an admin review.
const TOGGLE_TARGET: Partial<Record<RestaurantStatus, RestaurantStatus>> = {
  ACTIVE: 'SUSPENDED',
  SUSPENDED: 'ACTIVE',
};

const EMPTY_FORM: CreateRestaurantRequest = {
  name: '',
  cuisine: '',
  address: '',
  geo: { lat: 0, lng: 0 },
  openingHours: '',
};

export function RestaurantDashboard() {
  const queryClient = useQueryClient();
  const { restaurantId, setRestaurantId } = useSelectedRestaurant();
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<CreateRestaurantRequest>(EMPTY_FORM);

  const restaurantsQuery = useQuery({
    queryKey: ['restaurants', 'mine'],
    queryFn: myRestaurants,
  });

  // Default the active restaurant to the first one once loaded.
  const restaurants = restaurantsQuery.data;
  useEffect(() => {
    if (!restaurantId && restaurants && restaurants.length > 0) {
      setRestaurantId(restaurants[0].id);
    }
  }, [restaurantId, restaurants, setRestaurantId]);

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: RestaurantStatus }) =>
      setStatus(id, status),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['restaurants', 'mine'] });
    },
  });

  const createMutation = useMutation({
    mutationFn: (body: CreateRestaurantRequest) => createRestaurant(body),
    onSuccess: (created) => {
      setShowForm(false);
      setForm(EMPTY_FORM);
      setRestaurantId(created.id);
      void queryClient.invalidateQueries({ queryKey: ['restaurants', 'mine'] });
    },
  });

  if (restaurantsQuery.isLoading) {
    return <p className="ra-muted">Loading your restaurants…</p>;
  }
  if (restaurantsQuery.isError) {
    return <p className="ra-error">Could not load your restaurants.</p>;
  }

  const list = restaurants ?? [];

  return (
    <section>
      <div className="ra-row" style={{ marginBottom: '1rem' }}>
        <h2>Your restaurants</h2>
        <button
          type="button"
          className="ra-btn"
          onClick={() => setShowForm((v) => !v)}
        >
          {showForm ? 'Cancel' : 'New restaurant'}
        </button>
      </div>

      {showForm && (
        <NewRestaurantForm
          value={form}
          onChange={setForm}
          onSubmit={() => createMutation.mutate(form)}
          submitting={createMutation.isPending}
          error={createMutation.isError}
        />
      )}

      {list.length === 0 && !showForm && (
        <p className="ra-muted">
          You have no restaurants yet. Create one to start building a menu.
        </p>
      )}

      <div className="ra-grid">
        {list.map((r) => (
          <RestaurantCard
            key={r.id}
            restaurant={r}
            selected={r.id === restaurantId}
            onSelect={() => setRestaurantId(r.id)}
            onToggleStatus={() => {
              const target = TOGGLE_TARGET[r.status];
              if (target) {
                statusMutation.mutate({ id: r.id, status: target });
              }
            }}
            toggling={
              statusMutation.isPending &&
              statusMutation.variables?.id === r.id
            }
          />
        ))}
      </div>
    </section>
  );
}

function RestaurantCard({
  restaurant,
  selected,
  onSelect,
  onToggleStatus,
  toggling,
}: {
  restaurant: Restaurant;
  selected: boolean;
  onSelect: () => void;
  onToggleStatus: () => void;
  toggling: boolean;
}) {
  const target = TOGGLE_TARGET[restaurant.status];
  return (
    <article
      className="ra-card"
      style={selected ? { borderColor: 'var(--ra-accent)' } : undefined}
    >
      <div className="ra-row">
        <h3>{restaurant.name}</h3>
        <span className={`ra-badge ${STATUS_BADGE[restaurant.status]}`}>
          {restaurant.status}
        </span>
      </div>
      <p className="ra-muted">{restaurant.cuisine}</p>
      <p className="ra-muted">{restaurant.address}</p>
      <p className="ra-muted">Hours: {restaurant.openingHours || '—'}</p>
      <div className="ra-form-actions">
        <button
          type="button"
          className="ra-btn ra-btn--ghost"
          onClick={onSelect}
          disabled={selected}
        >
          {selected ? 'Selected' : 'Manage'}
        </button>
        {target && (
          <button
            type="button"
            className="ra-btn"
            onClick={onToggleStatus}
            disabled={toggling}
          >
            {target === 'ACTIVE' ? 'Reactivate' : 'Suspend'}
          </button>
        )}
      </div>
    </article>
  );
}

function NewRestaurantForm({
  value,
  onChange,
  onSubmit,
  submitting,
  error,
}: {
  value: CreateRestaurantRequest;
  onChange: (next: CreateRestaurantRequest) => void;
  onSubmit: () => void;
  submitting: boolean;
  error: boolean;
}) {
  const valid =
    value.name.trim() !== '' &&
    value.cuisine.trim() !== '' &&
    value.address.trim() !== '';

  return (
    <form
      className="ra-card"
      onSubmit={(e) => {
        e.preventDefault();
        if (valid) onSubmit();
      }}
    >
      <h3>New restaurant</h3>
      <div className="ra-field">
        <label htmlFor="r-name">Name</label>
        <input
          id="r-name"
          value={value.name}
          onChange={(e) => onChange({ ...value, name: e.target.value })}
        />
      </div>
      <div className="ra-field">
        <label htmlFor="r-cuisine">Cuisine</label>
        <input
          id="r-cuisine"
          value={value.cuisine}
          onChange={(e) => onChange({ ...value, cuisine: e.target.value })}
        />
      </div>
      <div className="ra-field">
        <label htmlFor="r-address">Address</label>
        <input
          id="r-address"
          value={value.address}
          onChange={(e) => onChange({ ...value, address: e.target.value })}
        />
      </div>
      <div className="ra-row">
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="r-lat">Latitude</label>
          <input
            id="r-lat"
            type="number"
            step="any"
            value={value.geo.lat}
            onChange={(e) =>
              onChange({
                ...value,
                geo: { ...value.geo, lat: Number(e.target.value) },
              })
            }
          />
        </div>
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="r-lng">Longitude</label>
          <input
            id="r-lng"
            type="number"
            step="any"
            value={value.geo.lng}
            onChange={(e) =>
              onChange({
                ...value,
                geo: { ...value.geo, lng: Number(e.target.value) },
              })
            }
          />
        </div>
      </div>
      <div className="ra-field">
        <label htmlFor="r-hours">Opening hours</label>
        <input
          id="r-hours"
          placeholder="Mon–Sun 10:00–22:00"
          value={value.openingHours}
          onChange={(e) => onChange({ ...value, openingHours: e.target.value })}
        />
      </div>
      {error && <p className="ra-error">Could not create the restaurant.</p>}
      <div className="ra-form-actions">
        <button type="submit" className="ra-btn" disabled={!valid || submitting}>
          {submitting ? 'Creating…' : 'Create restaurant'}
        </button>
      </div>
    </form>
  );
}
