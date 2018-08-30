import {Injectable} from '@angular/core';
import {isUndefined} from 'util';
import {Observable} from 'rxjs/Observable';
import {GenericDataService} from './genericdata.service';

import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';

import {Id} from '../model/id';
import {Password} from '../model/password';
import {User} from '../model/user';
import {UserRole, Role} from '../model/userrole';
import {UserSignup} from '../model/usersignup';
import {DomainService} from "./domain.service";

@Injectable()
export class UserService extends GenericDataService {

  constructor(http: HttpClient, appConfig: AppConfigService, protected domainService: DomainService) {
    super(http, appConfig);
  }

  public getAll(domainId?: number): Observable<User[]> {
    return this.get<User[]>(isUndefined(domainId) || domainId === this.domainService.getGlobalDomainId() ? this.getUsersUrl() : this.getDomainUsersUrl(domainId));
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

  public updateUser(userId: number, user: User): Observable<any> {
    return this.put<User, any>(this.getUsersUrl() + userId, user);
  }

  public changeUserStatus(userId: number, enabled: boolean): Observable<any> {
    return this.put(this.getEnableOrDisableUsersUrl(userId, enabled), {params: null});
  }

  public completeRegistration(user: User): Observable<any> {
    return this.post<User, any>(this.getUsersUrl()+'my/complete', user);
  }

  public addRole(userId: number, role: Role, domainId?: number): Observable<any> {
    const url: string = (isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId + '/roles';
    const targetDomainId: number = (isUndefined(domainId) ? this.appConfig.getNmaasGlobalDomainId() : domainId);

    return this.post<UserRole, UserRole>(url, new UserRole(targetDomainId, role));
  }

  public removeRole(userId: number, role: Role, domainId?: number): Observable<void> {
    return this.delete<void>((isUndefined(domainId) ? this.getUsersUrl() : this.getDomainUsersUrl(domainId)) + userId + '/roles/' + role);
  }

  public changePassword(passwordChange:Password): Observable<void> {
    return this.post<Password, void>(this.getUsersUrl() + 'my/auth/basic/password', passwordChange);
  }

  protected getUsersUrl(): string {
    return this.appConfig.getApiUrl() + '/users/';
  }

  protected getDomainUsersUrl(domainId: number): string {
    return this.appConfig.getApiUrl() + '/domains/' + domainId + '/users/';
  }

  protected getEnableOrDisableUsersUrl(userId: number, enabled: boolean): string {
      return this.appConfig.getApiUrl() + '/users/status/' + userId + '?enabled=' + enabled;
  }
}