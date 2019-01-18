
import {throwError as observableThrowError,  Observable } from 'rxjs';
import { Domain } from '../model/domain';
import { Registration } from '../model/registration';
import { AppConfigService } from '../service/appconfig.service';
import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {catchError, debounceTime} from 'rxjs/operators';

@Injectable()
export class RegistrationService {

  static headers = new HttpHeaders({'Content-Type': 'application/json', 'Accept': 'application/json'});
  
  constructor(private http: HttpClient, private appConfig: AppConfigService) {}
  
  public register(registration: Registration): Observable<any> {
    return this.http.post(this.getUrl(), registration, {headers: RegistrationService.headers}).pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }

  public getDomains(): Observable<Domain[]> {
    return this.http.get<Domain[]>(this.getUrl() + '/domains').pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }

  protected handleError(error: Response | any) {
    const errorMsg = error.error.message;
    return observableThrowError(errorMsg);
  }

  protected getUrl(): string {
    return this.appConfig.getApiUrl() + '/auth/basic/registration';
  }
  
}
