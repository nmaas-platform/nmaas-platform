import { Injectable } from '@angular/core';
import { AppConfigService } from '../service/appconfig.service';
import { JwtHelper } from 'angular2-jwt';
import { Http, Headers, Response, RequestOptions} from '@angular/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/map'

@Injectable()
export class AuthService {
  private jwtHelper: JwtHelper = new JwtHelper();
    
    
  constructor(private http: Http, private appConfig: AppConfigService) { }

  private storeToken(token: string):void {
    localStorage.setItem(this.appConfig.config.tokenName, token);
  }

  private getToken(): string {
      return localStorage.getItem(this.appConfig.config.tokenName)
  }
  
  private removeToken(): void {
      localStorage.removeItem(this.appConfig.config.tokenName);
  }
    
  public getUsername(): string {
    let token = this.getToken();
    return (token ? this.jwtHelper.decodeToken(token).sub : null);    
  }
  
  public getRoles(): string[] {
    let token = this.getToken();
    return (token ? this.jwtHelper.decodeToken(token).scopes : null);          
  }
  
  public hasRole(name: string): boolean {
    let token = this.getToken();
    return (token ? this.jwtHelper.decodeToken(token).scopes.indexOf(name) > -1 : null);                
  }
    
  public login(username: string, password: string) {
      let headers = new Headers({ 'Content-Type': 'application/json', 'Accept':'application/json' });
      return this.http.post(this.appConfig.config.apiUrl + '/api/auth/basic/login', JSON.stringify({ "username": username, "password": password }), new RequestOptions({ headers: headers }))
            .map((response: Response) => {
                // login successful if there's a jwt token in the response
                let token = response.json() && response.json().token;
                if (token) {
                    // set token property
                    this.storeToken(token);

                    return true;
                } else {
                    // return false to indicate failed login
                    return false;
                }
            });
  }
    
  public logout() : void  {
    this.removeToken();
  }
    
  public isLogged() : boolean {
    let token = this.getToken();
    return (token ? !this.jwtHelper.isTokenExpired(token) : false);
  }
}
