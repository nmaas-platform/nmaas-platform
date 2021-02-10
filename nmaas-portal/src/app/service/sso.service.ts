import { Injectable } from '@angular/core';
import {GenericDataService} from './genericdata.service';
import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';
import {Observable} from 'rxjs';
import {SSOConfig} from '../model/sso';

@Injectable()
export class SSOService extends GenericDataService {

  protected url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/auth/sso';
  }

  public getOne(): Observable<SSOConfig> {
    return this.get<SSOConfig>(this.url);
  }

}
