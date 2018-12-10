export class Domain {
  public id: number = undefined;
  public name: string = undefined;
  public codename: string = undefined;
  public active: boolean = undefined;
  public dcnConfigured = undefined;
  public kubernetesNamespace = undefined;
  public kubernetesStorageClass = undefined;
  public externalServiceDomain = undefined;
  
  constructor();  
  constructor(id?: number,
              name?: string,
              codename?: string,
              active?: boolean,
              dcnConfigured?:boolean,
              kubernetesNamespace?:string,
              kubernetesStorageClass?:string,
              externalServiceDomain?:string) {
    this.id = id;
    this.name = name;
    this.codename = codename;
    this.active = active;
    this.dcnConfigured = dcnConfigured;
    this.kubernetesNamespace = kubernetesNamespace;
    this.kubernetesStorageClass = kubernetesStorageClass;
    this.externalServiceDomain = externalServiceDomain;
  }
}
