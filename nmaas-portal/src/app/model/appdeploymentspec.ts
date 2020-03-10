import {AppDeploymentEnv} from "./appdeploymentenv";
import {ParameterType} from "./parametertype";
import {KubernetesTemplate} from "./kubernetestemplate";
import {AppAccessMethod} from "./app-access-method";

export class AppDeploymentSpec {
    public supportedDeploymentEnvironments: AppDeploymentEnv[] = [AppDeploymentEnv.KUBERNETES];
    public kubernetesTemplate: KubernetesTemplate = new KubernetesTemplate();
    public defaultStorageSpace: number = 1;
    public exposesWebUI: boolean = true;
    public deployParameters: Map<ParameterType, string> = new Map();
    public accessMethods: AppAccessMethod[] = [];
}
