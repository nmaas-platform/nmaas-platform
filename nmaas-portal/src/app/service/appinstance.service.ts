import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';

import {AppConfigService} from './appconfig.service';

import {Id, User} from '../model';
import {AppInstanceState, AppInstanceStatus} from '../model';
import {AppInstance, AppInstanceRequest} from '../model';
import {AppInstanceProgressStage} from '../model';
import {GenericDataService} from './genericdata.service';

import {Observable} from 'rxjs';

import {AppInstanceStateHistory} from '../model/app-instance-state-history';
import {AppConfiguration} from '../model/app-configuration';
import {map} from 'rxjs/operators';
import {AppInstanceExtended} from '../model/app-instance-extended';

function appInstanceSort(data: AppInstance[], sortColumn: string, sortDirection: string): AppInstance[] {
  data.sort((a, b) => {
    if (sortDirection === 'desc') {
      return a[sortColumn] < b[sortColumn] ? -1 : 1;
    } else {
      return a[sortColumn] > b[sortColumn] ? -1 : 1;
    }
  });
  return data;
}

@Injectable({
  providedIn: 'root',
})
export class AppInstanceService extends GenericDataService {

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public getSortedAllAppInstances(domainId: number, criteria?: CustomerSearchCriteria): Observable<AppInstance[]> {
    const options = {params: new HttpParams().set('sort', criteria.sortColumn + ',' + criteria.sortDirection)}
    return this.http.get<AppInstance[]>(this.getUrl() + 'domain/' + domainId, options).pipe(
        map(
            (data) => appInstanceSort(data, criteria.sortColumn, criteria.sortDirection)
        )
    )
  }

  public getSortedMyAppInstances(domainId: number, criteria?: CustomerSearchCriteria): Observable<AppInstance[]> {
    const options = {params: new HttpParams().set('sort', criteria.sortColumn + ',' + criteria.sortDirection)}
    return this.http.get<AppInstance[]>(this.getUrl() + 'domain/' + domainId + '/my', options).pipe(
        map(
            (data) => appInstanceSort(data, criteria.sortColumn, criteria.sortDirection)
      )
    )
  }

  public getAppInstanceState(id: number): Observable<AppInstanceStatus> {
    return this.get<AppInstanceStatus>(this.getUrl() + id + '/state');
  }

  public getAppInstanceHistory(id: number): Observable<AppInstanceStateHistory[]> {
    return this.get<AppInstanceStateHistory[]>(this.getUrl() + id + '/state/history');
  }

  public createAppInstance(domainId: number, appId: number, name: string): Observable<Id> {
    return this.post<AppInstanceRequest, Id>(this.getUrl() + 'domain/' + domainId, new AppInstanceRequest(appId, name));
  }

  public removeAppInstance(appInstanceId: number): Observable<any> {
    return this.delete<any>(this.getUrl() + appInstanceId);
  }

  public getAppInstance(appInstanceId: number): Observable<AppInstanceExtended> {
    return this.get<AppInstanceExtended>(this.getUrl() + appInstanceId);
  }

  public applyConfiguration(appInstanceId: number, configuration: AppConfiguration): Observable<void> {
    return this.post<AppConfiguration, any>(this.getUrl() + appInstanceId + '/configure', configuration);
  }

  public updateConfiguration(appInstanceId: number, configuration: AppConfiguration): Observable<void> {
      return this.post<AppConfiguration, any>(this.getUrl() + appInstanceId + '/configure/update', configuration);
  }

  public redeployAppInstance(appInstanceId: number): Observable<void> {
    return this.post<number, any>(this.getUrl() + appInstanceId + '/redeploy', appInstanceId);
  }

  public removeFailedInstance(appInstanceId: number): Observable<any> {
    return this.delete<any>(this.getUrl() + 'failed/' + appInstanceId);
  }

  public getConfiguration(appInstanceId: number): Observable<any> {
    return this.get<any>(this.getUrl() + appInstanceId + '/configuration');
  }

  protected getUrl(): string {
    return this.appConfig.getApiUrl() + '/apps/instances/';
  }

  public getProgressStages(): AppInstanceProgressStage[] {
    return [
      new AppInstanceProgressStage('Subscription validation', AppInstanceState.VALIDATION),
      new AppInstanceProgressStage('Environment creation', AppInstanceState.PREPARATION),
      new AppInstanceProgressStage('Setting up connectivity', AppInstanceState.CONNECTING),
      new AppInstanceProgressStage('Applying app configuration', AppInstanceState.CONFIGURATION_AWAITING),
      new AppInstanceProgressStage('App container deployment', AppInstanceState.DEPLOYING),
      new AppInstanceProgressStage('App running', AppInstanceState.RUNNING),
      new AppInstanceProgressStage('Undeploying', AppInstanceState.UNDEPLOYING, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE]),
      new AppInstanceProgressStage('Removed', AppInstanceState.DONE, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE])
    ];
  }

  public restartAppInstance(appInstanceId: number): Observable<any> {
    return this.post<number, any>((this.getUrl() +  appInstanceId + '/restart'), appInstanceId);
  }

  public getRunningAppInstances(domainId: number): Observable<AppInstance[]> {
    return this.get(this.getUrl() + 'running/domain/' + domainId);
  }

  public checkStatus(appInstanceId: number): Observable<any> {
    return this.post(this.getUrl() + appInstanceId + '/check', null);
  }

  public updateAppInstanceMembers(appInstanceId: number, members: User[]): Observable<void> {
    return this.http.post<void>(this.getUrl() + appInstanceId + '/members', members)
  }

  public getStatistics(): Observable<any> {
    return this.http.get(this.getUrl() + 'statistics')
  }

}

export class CustomerSearchCriteria {
  sortColumn: string;
  sortDirection: string;
}
