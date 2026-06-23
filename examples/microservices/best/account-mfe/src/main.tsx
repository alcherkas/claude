// Standalone remote entry. The async import keeps Module Federation's shared
// scope initialized before the federated App is consumed. When run on its own
// dev server (port 3004) this mounts the account MFE directly; inside the shell
// host the './App' export is consumed instead.
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
    console.error('[account] failed to bootstrap standalone application', err);
  });

export {};
