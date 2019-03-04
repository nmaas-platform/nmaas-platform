import {AppDeploymentEnv} from "./appdeploymentenv";
import {ParameterType} from "./parametertype";
import {KubernetesTemplate} from "./kubernetestemplate";

export class AppDeploymentSpec {
    public supportedDeploymentEnvironments: AppDeploymentEnv[] = [AppDeploymentEnv.KUBERNETES];
    public kubernetesTemplate: KubernetesTemplate = new KubernetesTemplate();
    public defaultStorageSpace: number = 1;
    public deployParameters: Map<ParameterType, string> = new Map();
    public configFileRepositoryRequired: boolean = false;
}