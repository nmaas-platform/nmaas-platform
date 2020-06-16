import {ConfigFileTemplate} from './configfiletemplate';

export class AppConfigurationSpec {
    public id: number = undefined;
    public configFileRepositoryRequired = false;
    public configUpdateEnabled = false;
    public templates: ConfigFileTemplate[] = [];
}
