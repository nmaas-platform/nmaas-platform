import {AppDeploymentEnv} from "./appdeploymentenv";
import {ParameterType} from "./parametertype";
import {KubernetesTemplate} from "./kubernetestemplate";

export class AppDeploymentSpec {
    public id: number;
    public supportedDeploymentEnvironments: AppDeploymentEnv[] = [AppDeploymentEnv.KUBERNETES];
    public kubernetesTemplate: KubernetesTemplate = new KubernetesTemplate();
    public defaultStorageSpace: number;
    public deployParameters: Map<ParameterType, string> = new Map();
    public configFileRepositoryRequired: boolean = false;
}