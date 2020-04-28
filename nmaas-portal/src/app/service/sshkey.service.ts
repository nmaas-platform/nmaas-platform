import { Injectable } from '@angular/core';
import {GenericDataService} from './genericdata.service';
import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';
import {Observable} from 'rxjs';
import {SSHKeyView} from '../model/sshkey-view';
import {SSHKeyRequest} from '../model/sshkey-request';

@Injectable({
  providedIn: 'root'
})
export class SSHKeyService extends GenericDataService{

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public getAll(): Observable<SSHKeyView[]> {
    return this.get<SSHKeyView[]>(this.appConfig.getApiUrl() + '/user/keys');
  }

  public createKey(request: SSHKeyRequest): Observable<any> {
    return this.put<SSHKeyRequest, any>(this.appConfig.getApiUrl() + '/user/keys', request);
  }

  public invalidate(id: number): Observable<any> {
    return this.delete(this.appConfig.getApiUrl() + '/user/keys/' + id);
  }
}
