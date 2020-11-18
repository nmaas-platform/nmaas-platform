export class Configuration {
    public id: number;
    public maintenance: boolean;
    public ssoLoginAllowed: boolean;
    public defaultLanguage: string;
    public testInstance: boolean;
    public sendAppInstanceFailureEmails: boolean;
    public appInstanceFailureEmailList: string[] = [];
}
