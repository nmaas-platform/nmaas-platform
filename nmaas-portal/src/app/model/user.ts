import {Role, UserRole} from './userrole';
import {SSHKeyView} from './sshkey-view';

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
  public selectedLanguage: string = undefined;

  public defaultDomain: number = undefined;

  public lastSuccessfulLoginDate: Date = undefined;
  public firstLoginDate: Date = undefined;

  public sshKeys: SSHKeyView[] = [];

  public getDomainIds(): number[] {
    return Array.from(new Set(this.roles.map(ur => ur.domainId)));
  }

  public getRoles(): Role[] {
    return Array.from(new Set(this.roles.map(ur => ur.role)));
  }

}
