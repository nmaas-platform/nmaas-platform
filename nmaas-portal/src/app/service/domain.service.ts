import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {GenericDataService} from './genericdata.service';

import {AuthHttp} from 'angular2-jwt';
import {AppConfigService} from './appconfig.service';

import {Id} from '../model/id';
import {Domain} from '../model/domain';
import {User} from '../model/user';
import {JsonMapperService} from './jsonmapper.service';

@Injectable()
export class DomainService extends GenericDataService {

  protected url: string;

  constructor(authHttp: AuthHttp, appConfig: AppConfigService, private jsonModelMapper: JsonMapperService) {
    super(authHttp, appConfig);

    this.url = this.appConfig.getApiUrl() + '/domains/';
  }

  public getGlobalDomainId(): number {
    return this.appConfig.getNmaasGlobalDomainId();
  }

  public getGlobalDomain(): Observable<Domain> {
    return this.getOne(this.getGlobalDomainId())
                .map((domain) => this.jsonModelMapper.deserialize(domain, Domain));
  }

  public getAll(): Observable<Domain[]> {
    return this.get<Domain[]>(this.url)
                .map((domains) => this.jsonModelMapper.deserialize(domains, Domain));
  }

  public getOne(domainId: number): Observable<Domain> {
    return this.get<Domain>(this.url + domainId)
                .map((domain) => this.jsonModelMapper.deserialize(domain, Domain));
  }

  public add(domain: Domain): Observable<Id> {
    return this.post<Domain, Id>(this.url, domain)
                .map((id) => this.jsonModelMapper.deserialize(id, Id));
  }

  public update(domain: Domain): Observable<any> {
    return this.put<Domain, Id>(this.url + domain.id, domain);
  }

  public remove(domainId: number): Observable<any> {
    return this.delete(this.url + domainId);
  }

  public getMyDomains(): Observable<Domain[]> {
    return this.get<Domain[]>(this.url + '/my').map((domain) => this.jsonModelMapper.deserialize(domain, Domain));
  }

  public getUsers(domainId: number): Observable<User[]> {
    return this.get<User[]>(this.url + '/users');
  }
}
