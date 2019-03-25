import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "rxjs";
import {Language} from "../model/language";

@Injectable()
export class InternationalizationService extends GenericDataService{

  private updateRequiredFlag: boolean = false;

  constructor(http:HttpClient, appConfig:AppConfigService) {
    super(http, appConfig);
  }

  public saveLanguageContent(language: Language) : Observable<any> {
    return this.post(this.getInternationalizationUrl()+ language.language + '?enabled=' + language.enabled, language);
  }

  public getAllSupportedLanguages():Observable<Language[]>{
    return this.get(this.getInternationalizationUrl() + 'brief/all');
  }

  public getAllSupportedLanguagesWithContent(): Observable<Language[]> {
    return this.get(this.getInternationalizationUrl() + 'all');
  }

  public changeSupportedLanguageState(language: Language):Observable<any>{
    return this.put(this.getInternationalizationUrl() + 'state', language);
  }

  public getEnabledLanguages(): Observable<string[]>{
    return this.get(this.getInternationalizationUrl() + 'all/enabled');
  }

  private getInternationalizationUrl(): string{
    return this.appConfig.getApiUrl() + '/i18n/';
  }

  public setUpdateRequiredFlag(flag:boolean){
    this.updateRequiredFlag = flag;
  }

  public shouldUpdate(): boolean{
    return this.updateRequiredFlag;
  }

}
