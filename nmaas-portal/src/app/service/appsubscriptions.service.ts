import { ApplicationMassive } from '../model/application-massive';
import { AppSubscription } from '../model/appsubscription';
import { AppConfigService } from './appconfig.service';
import { GenericDataService } from './genericdata.service';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { isUndefined } from 'util';

@Injectable()
export class AppSubscriptionsService extends GenericDataService {

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public subscribe(domainId: number, applicationId: number): Observable<any> {
    return this.post<AppSubscription, any>(this.getSubscriptionsUrl(), new AppSubscription(domainId, applicationId));
  }
  
  public subscribeRequest(domainId: number, applicationId: number): Observable<any> {
    return this.post<AppSubscription, any>(this.getSubscriptionsUrl()  + '/request', new AppSubscription(domainId, applicationId));
  }
  
  public unsubscribe(domainId: number, applicationId: number): Observable<any> {
    return this.delete<any>(this.getSubscriptionUrl(applicationId, domainId));
  }
  
  public getAll(): Observable<AppSubscription[]> {
    return this.get<AppSubscription[]>(this.getSubscriptionsUrl());
  }
  
  public getAllByApplication(applicationId: number): Observable<AppSubscription[]> {
    return this.get<AppSubscription[]>(this.getApplicationSubscriptionsUrl(applicationId));
  }

  public getAllByDomain(domainId: number): Observable<AppSubscription[]> {
    return this.get<AppSubscription[]>(this.getDomainSubscriptionsUrl(domainId));
  }
  
  public getSubscription(applicationId: number, domainId: number): Observable<AppSubscription> {
    return this.get<AppSubscription>(this.getSubscriptionUrl(applicationId, domainId));
  }
    
  public getSubscribedApplications(domainId?: number): Observable<ApplicationMassive[]> {
      return this.get<ApplicationMassive[]>(
                      (isUndefined(domainId) ? this.getSubscriptionsUrl() : this.getDomainSubscriptionsUrl(domainId)) + '/apps');
  }
  
  protected getUrl(): string {
    return this.appConfig.getApiUrl();
  }
    
  protected getSubscriptionsUrl(): string {
    return this.getUrl() + '/subscriptions';
  }
  
  protected getDomainSubscriptionsUrl(domainId: number): string {
    return this.getSubscriptionsUrl() + '/domains/' + domainId;
  }
  
  protected getApplicationSubscriptionsUrl(applicationId: number): string {
    return this.getSubscriptionsUrl() + '/apps/' + applicationId;
  }
  
  protected getSubscriptionUrl(applicationId: number, domainId: number): string {
    return this.getSubscriptionsUrl() + '/apps/' + applicationId + '/domains/' + domainId;
  }
  
}
