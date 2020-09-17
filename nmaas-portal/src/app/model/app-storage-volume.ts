import {ServiceStorageVolumeType} from "./service-storage-volume";

export class AppStorageVolume {
    public id: number;
    public type: ServiceStorageVolumeType;
    public defaultStorageSpace: number;
    public deployParameters: object = {}; // this should be Map<string, string> but JS cannot stringify object of this type
}
