import { Injectable } from '@angular/core';
import {AppConfigService} from "./appconfig.service";
import {HttpClient} from "@angular/common/http";
import {GenericDataService} from "./genericdata.service";
import {Observable} from "rxjs";
import {NmServiceConfigurationTemplate} from "../model/nmserviceconfigurationtemplate";

@Injectable({
  providedIn: 'root'
})
export class NmServiceConfigService extends GenericDataService {

  private readonly url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/management/configurations/templates'
  }

  public addAppTemplates(appId: number, configTemplates: NmServiceConfigurationTemplate[]) : Observable<any> {
    return this.post(this.url + '/' + appId, configTemplates);
  }

  public getAllTemplates(appId: number) : Observable<NmServiceConfigurationTemplate[]> {
    return this.get(this.url + '/' + appId);
  }

  public validateTemplates(configTemplates: NmServiceConfigurationTemplate[]) : Observable<any> {
    return this.post(this.url + '/validate', configTemplates);
  }

}
