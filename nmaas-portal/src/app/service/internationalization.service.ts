import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "rxjs";
import {Language} from "../model/language";

@Injectable()
export class InternationalizationService extends GenericDataService{

  constructor(http:HttpClient, appConfig:AppConfigService) {
    super(http, appConfig);
  }

  public getAllSupportedLanguages():Observable<Language[]>{
    return this.get(this.getInternationalizationUrl() + 'languages/all');
  }

  public changeSupportedLanguageState(language: Language):Observable<any>{
    return this.put(this.getInternationalizationUrl() + 'state', language);
  }

  private getInternationalizationUrl(): string{
    return this.appConfig.getApiUrl() + '/i18n/';
  }

}
