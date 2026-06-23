import { type FormEvent, useState } from 'react';
import type { CreateMenuItemRequest } from '../types';
import { parseMoneyToCents } from '../lib/money';

interface MenuItemFormProps {
  restaurantId: string;
  onSubmit: (body: CreateMenuItemRequest) => void;
  submitting: boolean;
  error: boolean;
}

interface FormState {
  name: string;
  description: string;
  price: string;
  currency: string;
  category: string;
  available: boolean;
}

const EMPTY: FormState = {
  name: '',
  description: '',
  price: '',
  currency: 'USD',
  category: '',
  available: true,
};

// Captures a new menu item and converts the human-entered major-unit price
// into integer cents before handing a CreateMenuItemRequest to the caller.
export function MenuItemForm({
  restaurantId,
  onSubmit,
  submitting,
  error,
}: MenuItemFormProps) {
  const [form, setForm] = useState<FormState>(EMPTY);

  const priceCents = parseMoneyToCents(form.price);
  const valid =
    form.name.trim() !== '' &&
    form.category.trim() !== '' &&
    priceCents !== null &&
    priceCents > 0;

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!valid || priceCents === null) return;
    onSubmit({
      restaurantId,
      name: form.name.trim(),
      description: form.description.trim(),
      priceCents,
      currency: form.currency,
      category: form.category.trim(),
      available: form.available,
    });
    setForm(EMPTY);
  }

  return (
    <form className="ra-card" onSubmit={handleSubmit}>
      <h3>Add menu item</h3>
      <div className="ra-field">
        <label htmlFor="m-name">Name</label>
        <input
          id="m-name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
      </div>
      <div className="ra-field">
        <label htmlFor="m-desc">Description</label>
        <textarea
          id="m-desc"
          rows={2}
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
      </div>
      <div className="ra-row">
        <div className="ra-field" style={{ flex: 1 }}>
          <label htmlFor="m-price">Price</label>
          <input
            id="m-price"
            inputMode="decimal"
            placeholder="9.50"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: e.target.value })}
          />
        </div>
        <div className="ra-field" style={{ width: 110 }}>
          <label htmlFor="m-currency">Currency</label>
          <select
            id="m-currency"
            value={form.currency}
            onChange={(e) => setForm({ ...form, currency: e.target.value })}
          >
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
          </select>
        </div>
      </div>
      <div className="ra-field">
        <label htmlFor="m-category">Category</label>
        <input
          id="m-category"
          placeholder="Mains, Sides, Drinks…"
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
        />
      </div>
      <div className="ra-field">
        <label htmlFor="m-available">
          <input
            id="m-available"
            type="checkbox"
            checked={form.available}
            onChange={(e) => setForm({ ...form, available: e.target.checked })}
          />{' '}
          Available immediately
        </label>
      </div>
      {form.price !== '' && priceCents === null && (
        <p className="ra-error">Enter a valid price.</p>
      )}
      {error && <p className="ra-error">Could not add the item.</p>}
      <div className="ra-form-actions">
        <button type="submit" className="ra-btn" disabled={!valid || submitting}>
          {submitting ? 'Adding…' : 'Add item'}
        </button>
      </div>
    </form>
  );
}
