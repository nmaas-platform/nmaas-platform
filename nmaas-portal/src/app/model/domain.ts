export class Domain {
  public id: number = undefined;
  public name: string = undefined;
  public codename: string = undefined;
  public active: boolean = undefined;
  
  constructor();  
  constructor(id?: number,
              name?: string,
              codename?: string,
              active?: boolean) { 
    this.id = id;
    this.name = name;
    this.codename = codename;
    this.active = active;
  }
}
