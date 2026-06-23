import { useQuery } from '@tanstack/react-query';
import { getMe, myReviews } from '../api/client';
import { formatDateTime } from '../format';

// The customer's submitted reviews — review-service GET /api/reviews?userId=.
export function ReviewList() {
  const { data: user } = useQuery({ queryKey: ['me'], queryFn: getMe });
  const userId = user?.id;

  const {
    data: reviews,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['reviews', userId],
    queryFn: () => myReviews(userId as string),
    enabled: Boolean(userId),
  });

  if (isLoading || !userId) {
    return <div className="acct-card acct-muted">Loading reviews…</div>;
  }

  if (isError || !reviews) {
    return (
      <div className="acct-card acct-error">Could not load your reviews.</div>
    );
  }

  if (reviews.length === 0) {
    return (
      <div className="acct-card acct-muted">
        You have not written any reviews yet. Reviews can be left from a
        delivered order.
      </div>
    );
  }

  return (
    <ul className="acct-reviews">
      {reviews.map((review) => (
        <li key={review.id} className="acct-card acct-review">
          <div className="acct-review__head">
            <span className="acct-stars" aria-label={`${review.rating} stars`}>
              {'★'.repeat(review.rating)}
              {'☆'.repeat(5 - review.rating)}
            </span>
            <span className="acct-review__date">
              {formatDateTime(review.createdAt)}
            </span>
          </div>
          <p className="acct-review__comment">{review.comment}</p>
          <p className="acct-review__meta">
            Order #{review.orderId.slice(0, 8)} · Restaurant{' '}
            {review.restaurantId.slice(0, 8)}
            {review.driverId ? ` · Courier ${review.driverId.slice(0, 8)}` : ''}
          </p>
        </li>
      ))}
    </ul>
  );
}
