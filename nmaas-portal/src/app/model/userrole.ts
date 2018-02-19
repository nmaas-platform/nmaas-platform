import {JsonObject, JsonConverter, JsonCustomConvert, JsonProperty} from 'json2typescript';

export enum Role {
  ROLE_SUPERADMIN,
  ROLE_DOMAIN_ADMIN,
  ROLE_TOOL_MANAGER,
  ROLE_USER,
  ROLE_GUEST
}

export function RoleAware(constructor: Function) {
    constructor.prototype.Role = Role;
}

@JsonConverter
export class RoleConverter implements JsonCustomConvert<Role> {
  serialize(data: Role): any {
    console.log('RoleConverter:serialize');
    return data;
  }
  deserialize(data: any): Role {
    console.log('RoleConverter:deserialize');
    return Role[Role[data]];
  }
}


@JsonObject
export class UserRole {
  @JsonProperty('domainId', Number)
  public domainId: number = undefined;

  @JsonProperty('role', RoleConverter)
  public role: Role  = undefined;
  
  constructor(domainId?: number, role?: Role) {
    this.domainId = domainId;
    this.role = role;
  }
}
