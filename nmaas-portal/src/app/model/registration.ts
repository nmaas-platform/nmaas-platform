export class Registration {
  private username: string = undefined;
  private password: string = undefined;
  private email: string = undefined;
  private firstname: string = undefined;
  private lastname: string = undefined;
  private domainId: number = undefined;
  private termsOfUseAccepted: boolean = undefined;
  private privacyPolicyAccepted: boolean = undefined;
  private language: string = undefined;

    constructor(username: string, password: string, email: string, firstname?: string,
                lastname?:string, domainId?: number, termsOfUseAccepted?: boolean,
                privacyPolicyAccepted?: boolean, language?: string) {
      this.username = username;
      this.password = password;
      this.email = email;
      this.firstname = firstname;
      this.lastname = lastname;
      this.domainId = domainId;
      this.termsOfUseAccepted = termsOfUseAccepted;
      this.privacyPolicyAccepted = privacyPolicyAccepted;
      this.language = language;
    }
}