export enum AppInstanceState {
  REQUESTED,
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
  public appInstanceId: number = undefined;
  public state: AppInstanceState = undefined;
  public previousState: AppInstanceState = undefined;
  public details: string = undefined;
  public userFriendlyDetails: string = undefined;
  public userFriendlyState: string = undefined;
}
