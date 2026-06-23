// Standalone remote entry. The async import keeps Module Federation's shared
// scope initialized before any shared singleton is consumed.
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
    console.error('[checkout] failed to bootstrap remote application', err);
  });

export {};
