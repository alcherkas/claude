// Standalone remote entry. The async import keeps Module Federation's shared
// scope initialized before any shared singleton (react / react-dom /
// react-router-dom) is consumed. When loaded inside the shell host the host
// imports './App' directly and this file is unused.
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
    console.error('[order-tracking] failed to bootstrap remote application', err);
  });

export {};
