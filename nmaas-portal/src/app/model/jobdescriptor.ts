import {ServiceType} from "./monitorentry";

export class JobDescriptor{
    public serviceName: ServiceType;
    public checkInterval: number;
}