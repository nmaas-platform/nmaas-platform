import {ConfigFileTemplate} from './configfiletemplate';

export class AppConfigurationSpec {
    public configFileRepositoryRequired = false;
    public configUpdateEnabled = false;
    public templates: ConfigFileTemplate[] = [];
}
