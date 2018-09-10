export enum ServiceType{
    GITLAB = "GitLab"
}

export enum MonitorStatus{
    SUCCESS,
    FAILURE
}

export enum TimeFormat {
    MIN,
    H
}


export class MonitorEntry {
    public id: number;
    public serviceName: ServiceType;
    public status: MonitorStatus;
    public lastCheck: Date;
    public lastSuccess: Date;
    public checkInterval: number;
    public timeFormat: TimeFormat = TimeFormat.MIN;
}