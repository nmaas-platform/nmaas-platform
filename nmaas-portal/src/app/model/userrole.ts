export enum Role {
  ROLE_SUPERADMIN,
  ROLE_OPERATOR,
  ROLE_DOMAIN_ADMIN,
  ROLE_TOOL_MANAGER,
  ROLE_USER,
  ROLE_GUEST
}

export function RoleAware(constructor: Function) {
    constructor.prototype.Role = Role;
}

export class UserRole {
  public domainId: number = undefined;

  public role: Role  = undefined;
  
  constructor(domainId?: number, role?: Role) {
    this.domainId = domainId;
    this.role = role;
  }
}
