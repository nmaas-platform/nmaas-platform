export enum ServiceType{
    GITLAB
}

export enum MonitorStatus{
    SUCCESS,
    FAILURE
}

export class MonitorEntry {
    public id: number;
    public serviceName: ServiceType;
    public status: MonitorStatus;
    public lastCheck: Date;
    public lastSuccess: Date;
    public checkInterval: number;
}