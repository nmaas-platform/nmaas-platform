export class UserSignup {

  public username: string = undefined;

  public domainId: number = undefined;

  public password: string = undefined;

  constructor(username: string, domainId?: number, password?: string) {
    this.username = username;
    this.password = password;
    this.domainId = domainId;
  }

}
