import {Role} from '../model/userrole';
import {Injectable} from '@angular/core';
import {AppConfigService} from '../service/appconfig.service';
import {JwtHelper} from 'angular2-jwt';
import {Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import {isUndefined} from 'util';
import {Authority} from '../model/authority';

export class DomainRoles {
  constructor(private domainId: number, private roles: string[] = []) {
  }

  public getDomainId(): number {
    return this.domainId;
  }

  public getRoles(): string[] {
    return this.roles;
  }

  public hasRole(role: string): boolean {
    return (this.roles != null ? this.roles.indexOf(role) >= 0 : false);
  }
}

@Injectable()
export class AuthService {
  private jwtHelper: JwtHelper = new JwtHelper();


  constructor(private http: Http, private appConfig: AppConfigService) {}

  private storeToken(token: string): void {
    localStorage.setItem(this.appConfig.config.tokenName, token);
  }

  private getToken(): string {
    return localStorage.getItem(this.appConfig.config.tokenName)
  }

  private removeToken(): void {
    localStorage.removeItem(this.appConfig.config.tokenName);
  }

  public getUsername(): string {
    const token = this.getToken();
    return (token ? this.jwtHelper.decodeToken(token).sub : null);
  }

  public hasRole(name: string): boolean {
    const token = this.getToken();
    const authorities: Authority[] = this.jwtHelper.decodeToken(token).scopes;
    for (let i = 0; i < authorities.length; i++) {
      if (authorities[i].authority.indexOf(name) > -1) {
        return true;
      }
    }
    return false;
  }

  public hasDomainRole(domainId: number, name: string): boolean {
    const token = this.getToken();
    const authorities: Authority[] = this.jwtHelper.decodeToken(token).scopes;
    for (let i = 0; i < authorities.length; i++) {
      if (authorities[i].authority.indexOf(domainId + ':' + name) > -1) {
        return true;
      }
    }
    return false;
  }

  public getDomainRoles(): Map<number, DomainRoles> {
    const drMap: Map<number, DomainRoles> = new Map<number, DomainRoles>();

    const token = this.getToken();
    if (token == null) {
      return drMap;
    }

    const authorities: Authority[] = this.jwtHelper.decodeToken(token).scopes;
    if (authorities == null) {
      return drMap;
    }

    for (let index = 0; index < authorities.length; index++) {
      if (isUndefined(authorities[index].authority)) {
        continue;
      }
      
      const domainRole: string[] = authorities[index].authority.split(':', 2);
      if (domainRole.length !== 2) {
        continue;
      }
      const domainId: number = Number.parseInt(domainRole[0]);
      const role: string = domainRole[1];

      let dr: DomainRoles;
      if (!drMap.has(domainId)) {
        drMap.set(domainId, new DomainRoles(domainId, []));
      }
      dr = drMap.get(domainId);
      dr.getRoles().push(role);
    }

    return drMap;
  }

  public getRoles(): string[] {
    const roles: string[] = [];

    const token = this.getToken();
    if (token == null) {
      return roles;
    }

    const authorities: Authority[]  = this.jwtHelper.decodeToken(token).scopes;
    for (let index = 0; index < authorities.length; index++) {
      if (isUndefined(authorities[index].authority)) {
        continue;
      }
      
      const domainRole: string[] = authorities[index].authority.split(':', 2);
      if (domainRole.length !== 2) {
        continue;
      }
      const role: string = domainRole[1];
      if (roles.indexOf(role) === -1) {
        roles.push(role);
      }
    }
    return roles;
  }


  public getDomains(): number[] {
    const domains: number[] = [];

    const token = this.getToken();
    if (token == null) {
      return domains;
    }

    const authorities: Authority[] = this.jwtHelper.decodeToken(token).scopes;
    
    for (let index = 0; index < authorities.length; index++) {
      if (isUndefined(authorities[index].authority)) {
        continue;
      }
      
      const domainIdStr: string[] = authorities[index].authority.split(':', 1);
      if (domainIdStr.length === 0) {
        continue;
      }
      const domainId: number = Number.parseInt(domainIdStr[0]);
      if (domains.indexOf(domainId) === -1) {
        domains.push(domainId);
      }
    }
    return domains;
  }

  public getDomainsWithRole(name: string): number[] {
    const domainsWithRole: number[] = [];

    const domains: number[] = this.getDomains();
    domains.forEach((domainId) => {
      if (this.hasDomainRole(domainId, name)) {
        domainsWithRole.push(domainId);
      }
    });

    return domainsWithRole;
  }

  public login(username: string, password: string): Observable<boolean> {
    const headers = new Headers({'Content-Type': 'application/json', 'Accept': 'application/json'});
    return this.http.post(this.appConfig.config.apiUrl + '/auth/basic/login',
      JSON.stringify({'username': username, 'password': password}), new RequestOptions({headers: headers}))
      .timeout(10000)
      .map((response: Response) => {
        console.debug('Login response: ' + response);
        // login successful if there's a jwt token in the response
        const token = response.json() && response.json().token;
        if (token) {
          // set token property
          this.storeToken(token);

          console.debug('AUTH | User: ' + this.getUsername());
          console.debug('AUTH | Domains: ' + this.getDomains());
          console.debug('AUTH | Roles: ' + this.getRoles());
          console.debug('AUTH | DomainRoles: ' + this.getDomainRoles());
          
          return true;
        } else {
          // return false to indicate failed login
          return false;
        }
      })
      .catch((error: Response | any) => {
        console.debug('Login error: ' + error);
        let errMsg: string;
        if (error instanceof Response) {
          console.debug(error.json());
          const body = error.json() || '';
          const err = body.message || JSON.stringify(body);
          errMsg = `${error.status} - ${err}`;
        } else {
          errMsg = 'Server error';
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
      });
  }

  public logout(): void {
    this.removeToken();
  }

  public isLogged(): boolean {
    const token = this.getToken();
    return (token ? !this.jwtHelper.isTokenExpired(token) : false);
  }
}
