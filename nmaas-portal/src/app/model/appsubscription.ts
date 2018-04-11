import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class AppSubscription {

  @JsonProperty('domainId', Number)
  public domainId: number = undefined;


  @JsonProperty('applicationId', Number)
  public applicationId: number = undefined;

  @JsonProperty('active', Boolean, true)
  public active: boolean = undefined;
  
  constructor(domainId?: number, applicationId?: number, active?: boolean) {
    this.domainId = domainId;
    this.applicationId = applicationId;
    this.active = active;
  }


}
