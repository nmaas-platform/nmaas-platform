import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "rxjs";
import {MailTemplate} from "../model/mailtemplate";

@Injectable({
  providedIn: 'root'
})
export class MailTemplateService extends GenericDataService {

  private readonly url: string;

  constructor(http: HttpClient, appConfigService: AppConfigService) {
    super(http, appConfigService);
    this.url = this.appConfig.getApiUrl() + '/mail/templates';
  }

  public getTemplates() : Observable<MailTemplate[]> {
    return this.get(this.url + '/all');
  }

  public saveTemplates(templates: MailTemplate[]) : Observable<any> {
    return this.patch(this.url + '/all', templates);
  }
}
