import {DcnDeploymentType} from "./dcndeploymenttype";

export class DomainDcnDetails {
    public id: number;
    public domainCodename: string;
    public dcnConfigured: boolean = false;
    public dcnDeploymentType: DcnDeploymentType = DcnDeploymentType.MANUAL;
}