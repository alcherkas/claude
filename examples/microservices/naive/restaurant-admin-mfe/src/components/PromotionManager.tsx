import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { createPromotion, promotions } from '../api/client';
import type { CreatePromotionRequest, Promotion } from '../types';
import { useSelectedRestaurant } from '../lib/selectedRestaurant';
import { formatMoney } from '../lib/money';
import { PromotionForm } from './PromotionForm';

function describeValue(promo: Promotion): string {
  switch (promo.type) {
    case 'PERCENT':
      return `${promo.value}% off`;
    case 'FIXED':
      return `${formatMoney(promo.value)} off`;
    case 'FREE_DELIVERY':
      return 'Free delivery';
    default:
      return '';
  }
}

export function PromotionManager() {
  const queryClient = useQueryClient();
  const { restaurantId } = useSelectedRestaurant();

  const promotionsQuery = useQuery({
    queryKey: ['promotions', restaurantId],
    queryFn: () => promotions(restaurantId as string),
    enabled: Boolean(restaurantId),
  });

  const createMutation = useMutation({
    mutationFn: (body: CreatePromotionRequest) => createPromotion(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['promotions', restaurantId],
      });
    },
  });

  if (!restaurantId) {
    return (
      <p className="ra-muted">
        Select a restaurant on the Dashboard to manage promotions.
      </p>
    );
  }
  if (promotionsQuery.isLoading) {
    return <p className="ra-muted">Loading promotions…</p>;
  }
  if (promotionsQuery.isError) {
    return <p className="ra-error">Could not load promotions.</p>;
  }

  const list = promotionsQuery.data ?? [];

  return (
    <section>
      <h2>Promotions</h2>

      <PromotionForm
        restaurantId={restaurantId}
        onSubmit={(body) => createMutation.mutate(body)}
        submitting={createMutation.isPending}
        error={createMutation.isError}
      />

      {list.length === 0 ? (
        <p className="ra-muted">No promotions yet. Create one above.</p>
      ) : (
        <div className="ra-grid">
          {list.map((promo) => (
            <PromotionCard key={promo.code} promo={promo} />
          ))}
        </div>
      )}
    </section>
  );
}

function PromotionCard({ promo }: { promo: Promotion }) {
  return (
    <article className="ra-card">
      <div className="ra-row">
        <h3>{promo.code}</h3>
        <span
          className={`ra-badge ${
            promo.active ? 'ra-badge--ok' : 'ra-badge--muted'
          }`}
        >
          {promo.active ? 'Active' : 'Inactive'}
        </span>
      </div>
      <p>
        <strong>{describeValue(promo)}</strong>
      </p>
      {promo.minSubtotalCents > 0 && (
        <p className="ra-muted">
          Min order {formatMoney(promo.minSubtotalCents)}
        </p>
      )}
      <p className="ra-muted">
        {new Date(promo.validFrom).toLocaleDateString()} –{' '}
        {new Date(promo.validTo).toLocaleDateString()}
      </p>
      <p className="ra-muted">
        Up to {promo.maxRedemptions} uses · {promo.perUserLimit} per customer
      </p>
    </article>
  );
}
