import {AppDeploymentEnv} from './app-deployment-env';
import {KubernetesTemplate} from './kubernetestemplate';
import {AppStorageVolume} from './app-storage-volume';
import {AppAccessMethod} from './app-access-method';

export class AppDeploymentSpec {
    public id: number = undefined;
    public supportedDeploymentEnvironments: AppDeploymentEnv[] = [AppDeploymentEnv.KUBERNETES];
    public kubernetesTemplate: KubernetesTemplate = new KubernetesTemplate();
    public exposesWebUI = true;
    public allowSshAccess = false;
    public deployParameters: object = {}; // this should be Map<ParameterType, string> = new Map(); but JS cannot stringify this
    public storageVolumes: AppStorageVolume[] = [];
    public accessMethods: AppAccessMethod[] = [];
    public globalDeployParameters: object = {}; // this should be Map<ParameterType, string> = new Map(); but JS cannot stringify this
}
