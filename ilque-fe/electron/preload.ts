import { contextBridge } from 'electron';

export interface ElectronAPI {
  readonly platform: NodeJS.Platform;
  readonly versions: {
    readonly node: string;
    readonly chrome: string;
    readonly electron: string;
  };
}

const electronAPI: ElectronAPI = {
  platform: process.platform,
  versions: {
    node: process.versions.node,
    chrome: process.versions.chrome,
    electron: process.versions.electron,
  },
};

contextBridge.exposeInMainWorld('electronAPI', electronAPI);
