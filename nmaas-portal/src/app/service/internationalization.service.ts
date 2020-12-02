import { Injectable } from '@angular/core';
import {GenericDataService} from './genericdata.service';
import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';
import {Observable} from 'rxjs';
import {Language} from '../model/language';

@Injectable({
  providedIn: 'root',
})
export class InternationalizationService extends GenericDataService {

  private basicLangs: string[];
  private updateRequiredFlag = false;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public saveLanguageContent(language: Language): Observable<any> {
    return this.patch(this.getInternationalizationUrl() + language.language, language.content);
  }

  public getAllSupportedLanguages(): Observable<Language[]> {
    return this.get(this.getInternationalizationUrl() + 'all');
  }

  public getLanguage(language: string): Observable<Language> {
    return this.get(this.getInternationalizationUrl() + language);
  }

  public changeSupportedLanguageState(language: Language): Observable<any> {
    return this.put(this.getInternationalizationUrl() + 'state', language);
  }

  public getEnabledLanguages(): Observable<string[]> {
    return this.get(this.getInternationalizationUrl() + 'all/enabled');
  }

  private getInternationalizationUrl(): string {
    return this.appConfig.getApiUrl() + '/i18n/';
  }

  public setUpdateRequiredFlag(flag: boolean) {
    this.updateRequiredFlag = flag;
  }

  public shouldUpdate(): boolean {
    return this.updateRequiredFlag;
  }

  public getLanguageContent(language: string): Observable<any> {
    return this.http.get(this.getInternationalizationUrl() + 'content/' + language);
  }

}
