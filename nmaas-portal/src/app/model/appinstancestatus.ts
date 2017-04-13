export enum AppInstanceState {
    SUBSCRIBED = 0, 
    VALIDATION = 1,
    PREPARATION = 2,
    CONNECTING = 3,
    CONFIGURATION_AWAITING = 4,
    DEPLOYING = 5,
    RUNNING = 6,
    UNDEPLOYING = 7,
    DONE = 8,
    FAILURE = 100,
    UNKNOWN = 200,    
}


export class AppInstanceStatus {
   
    appInstanceId: Number;
    state: AppInstanceState;
    details: string;
    
}