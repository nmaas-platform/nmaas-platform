export class Domain {
  public id: number = undefined;
  public name: string = undefined;
  public codename: string = undefined;
  public active: boolean = undefined;
  public kubernetesNamespace = undefined;
  public dcnConfigured = undefined;
  public persistentClass = undefined;
  
  constructor();  
  constructor(id?: number,
              name?: string,
              codename?: string,
              active?: boolean,
              kubernetesNamespace?:string,
              dcnConfigured?:boolean,
              persistentClass?:string) {
    this.id = id;
    this.name = name;
    this.codename = codename;
    this.active = active;
    this.kubernetesNamespace = kubernetesNamespace;
    this.dcnConfigured = dcnConfigured;
    this.persistentClass = persistentClass;
  }
}
