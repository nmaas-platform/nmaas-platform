import {User} from './user';
import {AppInstanceState} from './appinstancestatus';
import {ConfigTemplate} from "./configtemplate";

export class AppInstanceRequest {

  public applicationId: number = undefined;
  public name: string = undefined;

  constructor(applicationId?: number, name?: string) {
    this.applicationId = applicationId;
    this.name = name;
  }

}

export class AppInstance {

  public id: number = undefined;
  public domainId: number = undefined;
  public applicationId: number = undefined;
  public applicationName: string = undefined;
  public internalId:string = undefined;
  public name: string = undefined;
  public createdAt: Date = undefined;
  public owner: User = undefined;
  public configuration: string = undefined;
  public state: AppInstanceState = undefined;
  public userFriendlyState = undefined;
  public url: string = undefined;
  public configTemplate: ConfigTemplate = undefined;
}
