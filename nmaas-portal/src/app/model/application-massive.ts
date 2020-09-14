import {ConfigWizardTemplate} from './configwizardtemplate';
import {AppDeploymentSpec} from './app-deployment-spec';
import {ApplicationState} from './application-state';
import {AppConfigurationSpec} from './app-configuration-spec';
import {ApplicationBase} from './application-base';

export class ApplicationMassive extends ApplicationBase {
  public appVersionId: number = undefined;

  public version: string = undefined;

  public owner: string = undefined;

  public configWizardTemplate: ConfigWizardTemplate = undefined;
  public configUpdateWizardTemplate: ConfigWizardTemplate = undefined;
  public appDeploymentSpec: AppDeploymentSpec = new AppDeploymentSpec();
  public appConfigurationSpec: AppConfigurationSpec = new AppConfigurationSpec();

  public state: ApplicationState = ApplicationState.NEW;
}
