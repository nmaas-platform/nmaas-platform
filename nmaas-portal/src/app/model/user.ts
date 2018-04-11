import {Role, UserRole} from './userrole';
import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class User {

  @JsonProperty('id', Number)
  public id: number  = undefined;
  
  @JsonProperty('username', String)
  public username: string  = undefined;
  
  @JsonProperty('enabled', Boolean)
  public enabled: boolean = undefined;
  
  @JsonProperty('firstname', String, true)
  public firstname: string = undefined;
  
  @JsonProperty('lastname', String, true)
  public lastname: string = undefined;
  
  @JsonProperty('email', String, true)
  public email: string = undefined;
  
  @JsonProperty('roles', [UserRole])
  public userRoles: UserRole[] = [];
  
  public getDomainIds(): number[] {
    return Array.from(new Set(this.userRoles.map(ur => ur.domainId)));
  }

  public getRoles(): Role[] {
    return Array.from(new Set(this.userRoles.map(ur => ur.role)));
  }

}
