import {Role, UserRole} from './userrole';

export class User {
  public id: number  = undefined;
  public username: string  = undefined;
  public enabled: boolean = undefined;
  public firstname: string = undefined;
  public lastname: string = undefined;
  public email: string = undefined;
  public roles: UserRole[] = [];
  public termsOfUseAccepted: boolean = undefined;
  public privacyPolicyAccepted: boolean = undefined;
  public ssoUser: boolean = undefined;
  public selectedLanguage = undefined;
  
  public getDomainIds(): number[] {
    return Array.from(new Set(this.roles.map(ur => ur.domainId)));
  }

  public getRoles(): Role[] {
    return Array.from(new Set(this.roles.map(ur => ur.role)));
  }

}
