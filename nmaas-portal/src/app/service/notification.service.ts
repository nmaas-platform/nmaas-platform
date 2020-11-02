import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "rxjs";
import {Mail} from "../model/mail";

@Injectable({
  providedIn: 'root',
})
export class NotificationService extends GenericDataService{

  private readonly url:string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = appConfig.getApiUrl() + "/mail";
  }

  public sendMail(mail:Mail, token: string): Observable<any>{
    return this.post(this.url + "?token=" + token, mail);
  }

  public sendMailAdmin(mail: Mail): Observable<any> {
    return this.post(this.url + '/admin', mail);
  }

}
