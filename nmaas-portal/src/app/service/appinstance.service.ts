import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import {AppConfigService} from '../service/appconfig.service';

import {Id} from '../model/id';
import {AppInstanceState, AppInstanceStatus} from '../model/appinstancestatus';
import {AppInstance, AppInstanceRequest} from '../model/appinstance';
import {AppInstanceProgressStage} from '../model/appinstanceprogressstage';
import {GenericDataService} from './genericdata.service';

import {Observable} from 'rxjs';

import {AppInstanceStateHistory} from "../model/appinstancestatehistory";
import {AppConfiguration} from "../model/appconfiguration";
import {map} from 'rxjs/operators';

@Injectable()
export class AppInstanceService extends GenericDataService {

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public getSortedAllAppInstances(criteria?: CustomerSearchCriteria): Observable<AppInstance[]>{
    return this.get<AppInstance[]>(this.getUrl()).pipe(
        map(
      (data) => {
        data.sort((a, b) => {
          if(criteria.sortDirection === 'desc'){
            return a[criteria.sortColumn] < b[criteria.sortColumn] ? -1 : 1;
          }
          else {
            return a[criteria.sortColumn] > b[criteria.sortColumn] ? -1 : 1;
          }
        });
        return data;
      })
    )
  }

  public getSortedMyAppInstances(criteria?: CustomerSearchCriteria): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl() + 'my').pipe(
        map(
      (data) => {
        data.sort((a, b) => {
          if(criteria.sortDirection === 'desc'){
            return a[criteria.sortColumn] < b[criteria.sortColumn] ? -1 : 1;
          }
          else {
            return a[criteria.sortColumn] > b[criteria.sortColumn] ? -1 : 1;
          }
        });
        return data;
      })
    )
  }

  public getAppInstanceState(id: number): Observable<AppInstanceStatus> {
    return this.get<AppInstanceStatus>(this.getUrl() + id + '/state');
  }

  public getAppInstanceHistory(id:number): Observable<AppInstanceStateHistory[]>{
    return this.get<AppInstanceStateHistory[]>(this.getUrl()+id+ '/state/history');
  }

  public createAppInstance(domainId: number, appId: number, name: string): Observable<Id> {
    return this.post<AppInstanceRequest, Id>(this.getUrl() + "domain/" + domainId, new AppInstanceRequest(appId, name));
  }

  public removeAppInstance(appInstanceId: number): Observable<any> {
    return this.delete<any>(this.getUrl() + appInstanceId);
  }

  public getAppInstance(appInstanceId: number): Observable<AppInstance> {
    return this.get<AppInstance>(this.getUrl() + appInstanceId);
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
    return this.delete<any>(this.getUrl() + "failed/" + appInstanceId);
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

  public restartAppInstance(appInstanceId:number):Observable<any>{
    return this.post<number,any>((this.getUrl() +  appInstanceId + '/restart'), appInstanceId);
  }

  public getRunningAppInstances(domainId: number): Observable<AppInstance[]> {
    return this.get(this.getUrl() + "running/domain/" + domainId);
  }

}

export class CustomerSearchCriteria {
  sortColumn: string;
  sortDirection: string;
}