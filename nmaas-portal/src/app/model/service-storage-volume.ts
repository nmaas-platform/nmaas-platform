export enum ServiceStorageVolumeType {
    MAIN= 'MAIN',
    SHARED = 'SHARED'
}

export function parseServiceStorageVolumeType(arg: string | ServiceStorageVolumeType): ServiceStorageVolumeType {
    if (typeof arg === 'string') {
        return ServiceStorageVolumeType[arg];
    }
    return arg;
}
