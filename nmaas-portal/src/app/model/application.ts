import {ConfigTemplate} from './configtemplate';
import {AppDeploymentSpec} from "./appdeploymentspec";

export class Application {
  public id: number = undefined;
  public name: string = undefined;
  public version: string = undefined;
  public license: string = undefined;
  public wwwUrl: string = undefined;
  public sourceUrl: string = undefined;
  public issuesUrl: string = undefined;
  public briefDescription: string = undefined;
  public fullDescription: string = undefined;
  public tags: string[] = [];
  public configTemplate: ConfigTemplate = undefined;
  public additionalParametersTemplate: ConfigTemplate = undefined;
  public additionalMandatoryTemplate: ConfigTemplate = undefined;
  public appDeploymentSpec: AppDeploymentSpec = undefined;
}
