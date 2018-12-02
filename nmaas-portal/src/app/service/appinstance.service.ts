import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {isUndefined} from 'util';

import {AppConfigService} from '../service/appconfig.service';

import {Id} from '../model/id';
import {AppInstanceState, AppInstanceStatus} from '../model/appinstancestatus';
import {AppInstance, AppInstanceRequest} from '../model/appinstance';
import {AppInstanceProgressStage} from '../model/appinstanceprogressstage';
import {GenericDataService} from './genericdata.service';

import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import {AppInstanceStateHistory} from "../model/appinstancestatehistory";
import {AppConfiguration} from "../model/appconfiguration";
import {a, b} from "@angular/core/src/render3";

@Injectable()
export class AppInstanceService extends GenericDataService {

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public getAllAppInstances(domainId?: number): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId));
  }

  public getSortedAllAppInstances(domainId?: number, criteria?: CustomerSearchCriteria): Observable<AppInstance[]>{
    return this.get<AppInstance[]>(this.getUrl(domainId)).map(
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
      }
    )
  }

  public getMyAppInstances(domainId?: number): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId) + 'my');
  }

  public getSortedMyAppInstances(domainId?: number, criteria?: CustomerSearchCriteria): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId) + 'my').map(
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
      }
    )
  }

  public getUserAppInstances(username: string, domainId?: number): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId) + 'user/' + username);
  }

  public getAppInstanceState(id: number, domainId?: number): Observable<AppInstanceStatus> {
    return this.get<AppInstanceStatus>(this.getUrl(domainId) + id + '/state');
  }

  public getAppInstanceHistory(id:number, domainId?: number): Observable<AppInstanceStateHistory[]>{
    return this.get<AppInstanceStateHistory[]>(this.getUrl(domainId)+id+ '/state/history');
  }

  public createAppInstance(domainId: number, appId: number, name: string): Observable<Id> {
    return this.post<AppInstanceRequest, Id>(this.getUrl(domainId), new AppInstanceRequest(appId, name));
  }

  public removeAppInstance(appInstanceId: number, domainId?: number): Observable<any> {
    return this.delete<any>(this.getUrl(domainId) + appInstanceId);      
  }

  public getAppInstance(appInstanceId: number, domainId?: number): Observable<AppInstance> {
    return this.get<AppInstance>(this.getUrl(domainId) + appInstanceId);
  }

  public applyConfiguration(appInstanceId: number, configuration: AppConfiguration, domainId?: number): Observable<void> {
    return this.post<AppConfiguration, any>(this.getUrl(domainId) + appInstanceId + '/configure', configuration);
  }

  public updateConfiguration(appInstanceId: number, configuration: AppConfiguration, domainId?: number): Observable<void> {
      return this.post<AppConfiguration, any>(this.getUrl(domainId) + appInstanceId + '/configure/update', configuration);
  }

  public redeployAppInstance(appInstanceId: number, domainId?: number): Observable<void> {
    return this.post<number, any>(this.getUrl(domainId) + appInstanceId + '/redeploy', appInstanceId);
  }

  protected getUrl(domainId?: number): string {
    if (isUndefined(domainId)) {
      return this.appConfig.getApiUrl() + '/apps/instances/';
    } else {
      return this.appConfig.getApiUrl() + '/domains/' + domainId + '/apps/instances/';
    }
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

  public restartAppInstance(appInstanceId:number, domainId?: number):Observable<any>{
    return this.post<number,any>((this.getUrl(domainId) +  appInstanceId + '/restart'), appInstanceId);
  }

}

export class CustomerSearchCriteria {
  sortColumn: string;
  sortDirection: string;
}
