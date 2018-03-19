import {User} from './user';
import {AppInstanceState, AppInstanceStateConverter} from './appinstancestatus';
import {DateConverter} from './date';
import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class AppInstanceRequest {

  @JsonProperty('applicationId', Number)
  public applicationId: number = undefined;


  @JsonProperty('name', String)
  public name: string = undefined;

  constructor(applicationId?: number, name?: string) {
    this.applicationId = applicationId;
    this.name = name;
  }

}

@JsonObject
export class AppInstance {

  @JsonProperty('id', Number)
  public id: number = undefined;

  @JsonProperty('domainId', Number)
  public domainId: number = undefined;

  @JsonProperty('applicationId', Number)
  public applicationId: number = undefined;

  @JsonProperty('applicationName', String)
  public applicationName: string = undefined;

  @JsonProperty('name', String)
  public name: string = undefined;

  @JsonProperty('createdAt', DateConverter)
  public createdAt: Date = undefined;

  @JsonProperty('owner', User)
  public owner: User = undefined;

  @JsonProperty('configuration', String, true)
  public configuration: string = undefined;

  @JsonProperty('state', AppInstanceStateConverter)
  public state: AppInstanceState = undefined;

  @JsonProperty('url', String, true)
  public url: string = undefined;
}
