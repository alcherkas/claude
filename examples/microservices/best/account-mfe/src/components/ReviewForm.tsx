import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { submitReview } from '../api/client';
import type { Order, SubmitReviewRequest } from '../types';

interface ReviewFormProps {
  order: Order;
  onClose: () => void;
}

// Submit a review for a delivered order — review-service POST /api/reviews.
// Rendered as a modal from OrderHistory; on success it invalidates the user's
// review and order lists so both panels refresh.
export function ReviewForm({ order, onClose }: ReviewFormProps) {
  const queryClient = useQueryClient();
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');

  const mutation = useMutation({
    mutationFn: (body: SubmitReviewRequest) => submitReview(body),
    onSuccess: (created) => {
      queryClient.invalidateQueries({
        queryKey: ['reviews', created.userId],
      });
      queryClient.invalidateQueries({ queryKey: ['orders', order.userId] });
      onClose();
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    mutation.mutate({
      orderId: order.id,
      restaurantId: order.restaurantId,
      rating,
      comment: comment.trim(),
    });
  }

  return (
    <div className="acct-modal-backdrop" role="dialog" aria-modal="true">
      <div className="acct-modal">
        <header className="acct-modal__header">
          <h3>Review order #{order.id.slice(0, 8)}</h3>
          <button
            type="button"
            className="acct-modal__close"
            onClick={onClose}
            aria-label="Close"
          >
            ×
          </button>
        </header>

        <form className="acct-form" onSubmit={handleSubmit}>
          <label className="acct-field">
            Rating
            <select
              value={rating}
              onChange={(e) => setRating(Number(e.target.value))}
            >
              {[5, 4, 3, 2, 1].map((value) => (
                <option key={value} value={value}>
                  {value} star{value === 1 ? '' : 's'}
                </option>
              ))}
            </select>
          </label>

          <label className="acct-field">
            Comment
            <textarea
              rows={4}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="How was your meal?"
            />
          </label>

          {mutation.isError && (
            <p className="acct-form-error">
              Could not submit your review. Please try again.
            </p>
          )}

          <div className="acct-modal__footer">
            <button
              type="button"
              className="acct-btn acct-btn--ghost"
              onClick={onClose}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="acct-btn"
              disabled={mutation.isPending}
            >
              {mutation.isPending ? 'Submitting…' : 'Submit review'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
