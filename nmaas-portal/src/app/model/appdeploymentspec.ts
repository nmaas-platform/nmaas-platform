import {AppDeploymentEnv} from './appdeploymentenv';
import {ParameterType} from './parametertype';
import {KubernetesTemplate} from './kubernetestemplate';
import {AppStorageVolume} from './app-storage-volume';
import {AppAccessMethod} from './app-access-method';

export class AppDeploymentSpec {
    public supportedDeploymentEnvironments: AppDeploymentEnv[] = [AppDeploymentEnv.KUBERNETES];
    public kubernetesTemplate: KubernetesTemplate = new KubernetesTemplate();
    public exposesWebUI = true;
    public deployParameters: object = {}; // this should be Map<ParameterType, string> = new Map(); but JS cannot stringify this
    public storageVolumes: AppStorageVolume[] = [];
    public accessMethods: AppAccessMethod[] = [];
}
