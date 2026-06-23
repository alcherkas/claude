// Standalone host entry. The async import keeps Module Federation's shared
// scope initialized before any remote is consumed.
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
    console.error('[shell] failed to bootstrap host application', err);
  });

export {};
