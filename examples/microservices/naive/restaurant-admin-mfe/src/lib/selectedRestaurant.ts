import { createContext, useContext } from 'react';

// The admin features (menu, orders, promotions) operate on one "active"
// restaurant at a time. The dashboard chooses it and shares its id through this
// context so the feature pages don't each need to re-resolve it.
export interface SelectedRestaurant {
  restaurantId: string | null;
  setRestaurantId: (id: string) => void;
}

export const SelectedRestaurantContext = createContext<SelectedRestaurant>({
  restaurantId: null,
  setRestaurantId: () => {
    /* default no-op until a provider is mounted */
  },
});

export function useSelectedRestaurant(): SelectedRestaurant {
  return useContext(SelectedRestaurantContext);
}
