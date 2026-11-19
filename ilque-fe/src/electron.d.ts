export interface ElectronAPIVersions {
  readonly node: string;
  readonly chrome: string;
  readonly electron: string;
}

export interface ElectronAPI {
  readonly platform: NodeJS.Platform;
  readonly versions: ElectronAPIVersions;
}

declare global {
  interface Window {
    readonly electronAPI?: ElectronAPI;
  }
}
