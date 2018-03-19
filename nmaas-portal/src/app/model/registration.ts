
import { JsonObject, JsonProperty } from 'json2typescript';

@JsonObject
export class Registration {
    
    @JsonProperty('username', String)
    public username: string = undefined;
 
    @JsonProperty('password', String)
    public password: string = undefined;
  
    @JsonProperty('email', String)
    public email: string = undefined;
  
    @JsonProperty('firstname', String, true)
    public firstname: string = undefined;

    @JsonProperty('lastname', String, true)
    public lastname: string = undefined;
  
    @JsonProperty('domainId', Number, true)
    public domainId: number = undefined;
  

    constructor(username: string, password: string, email: string, firstname?: string, lastname?:string, domainId?: number) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.firstname = firstname;
      this.lastname = lastname;
      this.domainId = domainId;
    }
}