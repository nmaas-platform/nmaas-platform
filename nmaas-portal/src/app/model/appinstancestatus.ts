export enum AppInstanceState {
    SUBSCRIBED, 
    VALIDATION,
    PREPARATION,
    CONNECTING,
    CONFIGURATION_AWAITING,
    DEPLOYING,
    RUNNING,
    UNDEPLOYING,
    DONE,
    FAILURE,
    UNKNOWN    
}


export class AppInstanceStatus {
    
   constructor(
    public appInstanceId?: Number,
    public state?: AppInstanceState,
    public details?: string) {}
    
}

export function AppInstanceStateAware(constructor: Function) {
    constructor.prototype.AppInstanceState = AppInstanceState;
}