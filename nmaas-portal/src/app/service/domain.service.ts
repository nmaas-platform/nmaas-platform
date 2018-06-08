import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {GenericDataService} from './genericdata.service';

import {HttpClient} from '@angular/common/http'
import {AppConfigService} from './appconfig.service';

import {Id} from '../model/id';
import {Domain} from '../model/domain';
import {User} from '../model/user';

@Injectable()
export class DomainService extends GenericDataService {

  protected url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);

    this.url = this.appConfig.getApiUrl() + '/domains/';
  }

  public getGlobalDomainId(): number {
    return this.appConfig.getNmaasGlobalDomainId();
  }

  public getGlobalDomain(): Observable<Domain> {
    return this.getOne(this.getGlobalDomainId());
  }

  public getAll(): Observable<Domain[]> {
    return this.get<Domain[]>(this.url);
  }

  public getOne(domainId: number): Observable<Domain> {
    return this.get<Domain>(this.url + domainId);
  }

  public add(domain: Domain): Observable<Id> {
    return this.post<Domain, Id>(this.url, domain);
  }

  public update(domain: Domain): Observable<any> {
    return this.put<Domain, Id>(this.url + domain.id, domain);
  }

  public remove(domainId: number): Observable<any> {
    return this.delete(this.url + domainId);
  }

  public getMyDomains(): Observable<Domain[]> {
    return this.get<Domain[]>(this.url + 'my');
  }

  public getUsers(domainId: number): Observable<User[]> {
    return this.get<User[]>(this.url + 'users');
  }
}
