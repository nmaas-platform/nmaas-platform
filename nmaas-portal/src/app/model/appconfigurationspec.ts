import {ConfigFileTemplate} from "./configfiletemplate";

export class AppConfigurationSpec{
    public configFileRepositoryRequired: boolean = false;
    public templates: ConfigFileTemplate[] = [];
}