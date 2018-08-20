export class Registration {
    public username: string = undefined;
    public password: string = undefined;
    public email: string = undefined;
    public firstname: string = undefined;
    public lastname: string = undefined;
    public domainId: number = undefined;
    public touAccept: boolean = undefined;
  

    constructor(username: string, password: string, email: string, firstname?: string, lastname?:string, domainId?: number, touAccept?: boolean) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.firstname = firstname;
      this.lastname = lastname;
      this.domainId = domainId;
      this.touAccept = touAccept;
    }
}