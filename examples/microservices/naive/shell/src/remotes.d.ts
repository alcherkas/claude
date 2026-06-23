// Ambient declarations for the federated remote modules consumed by the host.
// Each feature MFE exposes './App' as a default React component
// (PLATFORM_SPEC §2.3). Federation names match vite.config.ts remotes.

declare module 'discovery/App' {
  import type { ComponentType } from 'react';
  const App: ComponentType;
  export default App;
}

declare module 'checkout/App' {
  import type { ComponentType } from 'react';
  const App: ComponentType;
  export default App;
}

declare module 'orderTracking/App' {
  import type { ComponentType } from 'react';
  const App: ComponentType;
  export default App;
}

declare module 'account/App' {
  import type { ComponentType } from 'react';
  const App: ComponentType;
  export default App;
}

declare module 'restaurantAdmin/App' {
  import type { ComponentType } from 'react';
  const App: ComponentType;
  export default App;
}

declare module 'driverPortal/App' {
  import type { ComponentType } from 'react';
  const App: ComponentType;
  export default App;
}
