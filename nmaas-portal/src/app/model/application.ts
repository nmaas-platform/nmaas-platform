import {ConfigTemplate} from './configtemplate';
import {AppDeploymentSpec} from "./appdeploymentspec";
import {AppDescription} from "./appdescription";

export class Application {
  public id: number = undefined;
  public name: string = undefined;
  public version: string = undefined;
  public license: string = undefined;
  public licenseUrl: string = undefined;
  public wwwUrl: string = undefined;
  public sourceUrl: string = undefined;
  public issuesUrl: string = undefined;
  public descriptions: AppDescription[] = [];
  public tags: string[] = [];
  public configurationUpdateTemplate: ConfigTemplate = undefined;
  public appDeploymentSpec: AppDeploymentSpec = undefined;
  public deleted: boolean = false;
}
