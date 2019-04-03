import {DomainDcnDetails} from "./domaindcndetails";
import {DomainTechDetails} from "./domaintechdetails";

export class Domain {
  public id: number = undefined;
  public name: string = undefined;
  public codename: string = undefined;
  public active: boolean = undefined;
  public domainDcnDetails: DomainDcnDetails = undefined;
  public domainTechDetails: DomainTechDetails = undefined;
  
  constructor();  
  constructor(id?: number,
              name?: string,
              codename?: string,
              active?: boolean,
              domainDcnDetails?: DomainDcnDetails,
              domainTechDetails?: DomainTechDetails) {
    this.id = id;
    this.name = name;
    this.codename = codename;
    this.active = active;
    this.domainDcnDetails = domainDcnDetails;
    this.domainTechDetails = domainTechDetails
  }
}
