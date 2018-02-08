import {Role, UserRole} from './userrole';

export class User {

  constructor(public id: number, public username: string, public userRoles: UserRole[]) {
  }

  public getDomainIds(): number[] {
    return this.userRoles.map(ur => ur.domainId);
  }

  public getRoles(): Role[] {
    return Array.from(new Set(this.userRoles.map(ur => ur.role)));
  }

}
