import {DomainDcnDetails} from "./domaindcndetails";
import {DomainTechDetails} from "./domaintechdetails";

export class Domain {
  public id: number = undefined;
  public name: string = undefined;
  public codename: string = undefined;
  public active: boolean = undefined;
  public domainDcnDetails: DomainDcnDetails = new DomainDcnDetails();
  public domainTechDetails: DomainTechDetails = new DomainTechDetails();
}
