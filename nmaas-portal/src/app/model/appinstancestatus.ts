import {JsonConverter, JsonCustomConvert, JsonObject, JsonProperty} from 'json2typescript';

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

export function AppInstanceStateAware(constructor: Function) {
  constructor.prototype.AppInstanceState = AppInstanceState;
}

@JsonConverter
export class AppInstanceStateConverter implements JsonCustomConvert<AppInstanceState> {
  serialize(data: AppInstanceState): any {
    console.log('AppInstanceStateConverter:serialize');
    return data;
  }
  deserialize(data: any): AppInstanceState {
    console.log('AppInstanceStateConverter:deserialize');
    return AppInstanceState[AppInstanceState[data]];
  }
}

@JsonObject
export class AppInstanceStatus {

  @JsonProperty('appInstanceId', Number)
  public appInstanceId: number = undefined;

  @JsonProperty('state', AppInstanceStateConverter)
  public state: AppInstanceState = undefined;

  @JsonProperty('details', String)
  public details: string = undefined;


}
