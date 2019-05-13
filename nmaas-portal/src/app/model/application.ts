import {ConfigWizardTemplate} from './configwizardtemplate';
import {AppDeploymentSpec} from "./appdeploymentspec";
import {AppDescription} from "./appdescription";
import {ApplicationState} from "./applicationstate";
import {AppConfigurationSpec} from "./appconfigurationspec";
import {ApplicationVersion} from "./applicationversion";

export class Application {
  public id: number = undefined;
  public appVersionId: number = undefined;
  public name: string = undefined;
  public version: string = undefined;
  public license: string = undefined;
  public licenseUrl: string = undefined;
  public wwwUrl: string = undefined;
  public sourceUrl: string = undefined;
  public issuesUrl: string = undefined;
  public owner: string = undefined;
  public descriptions: AppDescription[] = [];
  public tags: string[] = [];
  public appVersions: ApplicationVersion[] = [];
  public configWizardTemplate: ConfigWizardTemplate = undefined;
  public configUpdateWizardTemplate: ConfigWizardTemplate = undefined;
  public appDeploymentSpec: AppDeploymentSpec = new AppDeploymentSpec();
  public appConfigurationSpec: AppConfigurationSpec = new AppConfigurationSpec();
  public state: ApplicationState = ApplicationState.NEW;
}
