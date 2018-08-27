export class ClusterInfo {
    public id: number;
}

class ClusterApi {
    public id: number;
    public restApiHostAddress: string;
    public restApiPort: number;
}

class ClusterAttachPoint {
    public id: number;
    public routerId: string="";
    public routerInterfaceName: string="";
    public routerName: string="";
}

class ClusterDeployment {
    public defaultNamespace: string="";
    public defaultStorageClass: string="";
    public id: number;
    public namespaceConfigOption: string;
    public useInClusterGitLabInstance: boolean = false;
}

export class ClusterExtNetwork {
    public assigned: boolean = false;
    public assignedSince: string;
    public assignedTo: string;
    public externalIp: string;
    public externalNetwork: string;
    public externalNetworkMaskLength: number;
    public id: number;
}

class ClusterHelm {
    public helmHostAddress: string;
    public helmHostChartsDirectory: string;
    public helmHostSshUsername: string;
    public helmChartRepositoryName: string;
    public id: number;
    public useLocalChartArchives: boolean = false;
}

class ClusterIngress {
    public id: number;
    public controllerConfigOption: string;
    public controllerChartName: string;
    public controllerChartArchive: string;
    public resourceConfigOption: string;
    public externalServiceDomain: string;
    public tlsSupported: boolean = false;
    public supportedIngressClass: string;
}

export class Cluster {
    public api: ClusterApi;
    public attachPoint: ClusterAttachPoint;
    public deployment: ClusterDeployment;
    public externalNetworks: ClusterExtNetwork[];
    public helm: ClusterHelm;
    public id: number;
    public ingress: ClusterIngress;
    constructor(){
        this.api = new ClusterApi();
        this.attachPoint = new ClusterAttachPoint();
        this.deployment = new ClusterDeployment();
        this.externalNetworks = [];
        this.helm = new ClusterHelm();
        this.ingress = new ClusterIngress();
    }
}

export enum IngressControllerConfigOption{
    USE_EXISTING = 'USE_EXISTING',
    DEPLOY_NEW_FROM_REPO = 'DEPLOY_NEW_FROM_REPO',
    DEPLOY_NEW_FROM_ARCHIVE = 'DEPLOY_NEW_FROM_ARCHIVE'
}

export enum IngressResourceConfigOption{
    NOT_USED = 'NOT_USED',
    DEPLOY_USING_API = 'DEPLOY_USING_API',
    DEPLOY_FROM_CHART = 'DEPLOY_FROM_CHART'
}

export enum NamespaceConfigOption{
    USE_DEFAULT_NAMESPACE = 'USE_DEFAULT_NAMESPACE',
    USE_DOMAIN_NAMESPACE = 'USE_DOMAIN_NAMESPACE',
    CREATE_NAMESPACE = 'CREATE_NAMESPACE'
}