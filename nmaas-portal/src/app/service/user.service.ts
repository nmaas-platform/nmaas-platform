import {Injectable} from '@angular/core';
import {isUndefined} from 'util';
import {Observable} from 'rxjs/Observable';
import {GenericDataService} from './genericdata.service';

import {AuthHttp} from 'angular2-jwt';
import {AppConfigService} from './appconfig.service';

import {Id} from '../model/id';
import { Password } from '../model/password';
import {User} from '../model/user';
import {UserRole, Role} from '../model/userrole';

@Injectable()
export class UserService extends GenericDataService {

  constructor(authHttp: AuthHttp, appConfig: AppConfigService) {
    super(authHttp, appConfig);
  }

  public getAll(domainId?: number): Observable<User[]> {
    return this.get<User[]>(isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId));
  }

  public getOne(userId: number, domainId?: number): Observable<User> {
    return this.get<User>((isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId);
  }

  public deleteOne(userId: number, domainId?: number): Observable<any> {
    return this.delete<any>((isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId);
  }

  public getRoles(userId: number, domainId?: number): Observable<UserRole[]> {
    return this.get<UserRole[]>((isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId + '/roles');
  }

  public addRole(userId: number, role: Role, domainId?: number): Observable<any> {
    const url: string = (isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId + '/roles';
    const targetDomainId: number = (isUndefined(domainId) ? this.appConfig.getNmaasGlobalDomainId() : domainId);

    return this.post<any, UserRole>(url, JSON.stringify({'domainId': targetDomainId, 'role': role}));
  }

  public removeRole(userId: number, role: Role, domainId?: number): Observable<any> {
    return this.delete<any>((isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId + '/roles/' + role);
  }

  public changePassword(userId: number, password: string): Observable<any> {
    return this.post<Password, any>(this.getUsersUrl() + userId, new Password(password));
  }

  protected getUsersUrl(): string {
    return this.appConfig.getApiUrl() + '/users/';
  }

  protected getDomainUsersUrl(domainId: number): string {
    return this.appConfig.getApiUrl() + '/domains/' + domainId + '/users/';
  }

}
