import {ConfigWizardTemplate} from './configwizardtemplate';
import {AppDeploymentSpec} from './app-deployment-spec';
import {AppConfigurationSpec} from './app-configuration-spec';
import {ApplicationState} from './application-state';

export class Application {
    public id: number = undefined;

    public name: string = undefined
    public version: string = undefined;

    public configWizardTemplate: ConfigWizardTemplate = undefined;
    public configUpdateWizardTemplate: ConfigWizardTemplate = undefined;
    public appDeploymentSpec: AppDeploymentSpec = new AppDeploymentSpec();
    public appConfigurationSpec: AppConfigurationSpec = new AppConfigurationSpec();

    public owner: string = undefined;
    public state: ApplicationState = ApplicationState.NEW;
}
