
export enum Role {
  ROLE_SUPERADMIN,
  ROLE_DOMAIN_ADMIN,
  ROLE_TOOL_MANAGER,
  ROLE_USER,
  ROLE_GUEST
}

export class UserRole {
  constructor(public domainId: number, public role: Role) {}
}
