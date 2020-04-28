export enum ServiceStorageVolumeType {
    MAIN= 'MAIN',
    SHARED = 'SHARED'
}

export class ServiceStorageVolume {

    public static getServiceStorageVolumeTypeAsEnum(arg: string | ServiceStorageVolumeType): ServiceStorageVolumeType {
        if (typeof arg === 'string') {
            return ServiceStorageVolumeType[arg];
        }
        return arg;
    }

}