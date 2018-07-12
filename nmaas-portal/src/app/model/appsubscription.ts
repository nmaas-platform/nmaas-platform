export class AppSubscription {
  public domainId: number = undefined;
  public applicationId: number = undefined;
  public active: boolean = undefined;
  
  constructor(domainId?: number, applicationId?: number, active?: boolean) {
    this.domainId = domainId;
    this.applicationId = applicationId;
    this.active = active;
  }


}
