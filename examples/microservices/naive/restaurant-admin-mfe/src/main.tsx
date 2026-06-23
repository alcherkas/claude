// Standalone remote entry. The async import keeps Module Federation's shared
// scope initialized before any shared singleton (react / react-router-dom) is
// touched — mirrors the host's bootstrap strategy.
import('./bootstrap')
  .then(({ mount }) => {
    const container = document.getElementById('root');
    if (!container) {
      throw new Error('Root container #root not found');
    }
    mount(container);
  })
  .catch((err) => {
    // eslint-disable-next-line no-console
    console.error('[restaurantAdmin] failed to bootstrap standalone app', err);
  });

export {};
