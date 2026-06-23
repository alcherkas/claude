import { Component, type ErrorInfo, type ReactNode } from 'react';

interface RemoteBoundaryProps {
  name: string;
  children: ReactNode;
}

interface RemoteBoundaryState {
  hasError: boolean;
}

// Catches load/render failures of a federated remote so a single offline MFE
// never takes down the whole host shell.
export class RemoteBoundary extends Component<
  RemoteBoundaryProps,
  RemoteBoundaryState
> {
  state: RemoteBoundaryState = { hasError: false };

  static getDerivedStateFromError(): RemoteBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    // eslint-disable-next-line no-console
    console.error(
      `[shell] failed to load remote "${this.props.name}"`,
      error,
      info.componentStack,
    );
  }

  render(): ReactNode {
    if (this.state.hasError) {
      return (
        <div className="qb-remote-error">
          <h2>This section is unavailable</h2>
          <p>
            The <strong>{this.props.name}</strong> module could not be loaded.
            Make sure its dev server / bundle is running, then retry.
          </p>
          <button
            type="button"
            className="qb-btn"
            onClick={() => this.setState({ hasError: false })}
          >
            Retry
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
