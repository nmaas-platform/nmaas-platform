export class ClusterInfo {
    public helmHostAddress: string;
    public name: String;
    public restApiHostAddress: string;
    public restApiPort: number;
}

class ClusterApi {
    public id: number;
    public restApiHostAddress: string;
    public restApiPort: number;
}

class ClusterAttachPoint {
    public id: number;
    public routerId: string;
    public routerInterfaceName: string;
    public routerName: string;
}

class ClusterDeployment {
    public defaultNamespace: string;
    public defaultPersistenceClass: string;
    public id: number;
    public useDefaultNamespace: boolean;
}

class ClusterExtNetwork {
    public assigned: boolean;
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
    public id: number;
    public useLocalChartArchives: boolean;
}

class ClusterIngress {
    public controllerChartArchive: string;
    public externalServiceDomain: string;
    public id: number;
    public supportedIngressClass: string;
    public tlsSupported: boolean;
    public useExistingController: boolean;
    public useExistingIngress: boolean;
}

export class Cluster {
    public api: ClusterApi;
    public attachPoint: ClusterAttachPoint;
    public deployment: ClusterDeployment;
    public externalNetworks: ClusterExtNetwork[];
    public helm: ClusterHelm;
    public id: number;
    public ingress: ClusterIngress;
    public name: string;
}