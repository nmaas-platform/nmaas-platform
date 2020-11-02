import { Injectable } from '@angular/core';
import {GenericDataService} from './genericdata.service';
import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';
import {Observable} from 'rxjs';
import {GitLabConfig} from '../model/gitlab';

@Injectable({
  providedIn: 'root',
})
export class GitlabService extends GenericDataService {

  protected url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/management/gitlab/';
  }

  public getAll(): Observable<GitLabConfig[]> {
    return this.get<GitLabConfig[]>(this.url);
  }

  public getOne(config_id: number): Observable<GitLabConfig> {
    return this.get<GitLabConfig>(this.url + config_id);
  }

  public add(gitLabConfig: GitLabConfig): Observable<any> {
    return this.post(this.url, gitLabConfig);
  }

  public update(gitLabConfig: GitLabConfig): Observable<any> {
    return this.put(this.url + gitLabConfig.id, gitLabConfig);
  }

  public remove(config_id: number): Observable<any> {
    return this.delete(this.url + config_id);
  }


}
