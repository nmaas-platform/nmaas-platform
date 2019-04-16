import {LanguageMailContent} from "./languagemailcontent";

export class MailTemplate {
    public mailType: string;
    public globalInformation: any;
    public templates: LanguageMailContent[] = [];
}