
import {throwError as observableThrowError, Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import {AppConfigService} from './appconfig.service';
import {catchError, debounceTime} from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class GenericDataService {

  constructor(protected http: HttpClient, protected appConfig: AppConfigService) {}

  protected get<T>(url: string): Observable<T> {
    return this.http.get<T>(url).pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }

  protected post<S, T>(url: string, entity: S): Observable<T> {
    return this.http.post<T>(url, entity).pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }

  protected put<S, T>(url: string, entity: S): Observable<T> {
    return this.http.put<T>(url, entity).pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }
  protected patch<S, T>(url: string, entity: S): Observable<T> {
    return this.http.patch<T>(url, entity).pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }

  protected delete<T>(url: string): Observable<T> {
    return this.http.delete<T>(url).pipe(
        debounceTime(this.appConfig.getHttpTimeout()), catchError(this.handleError));
  }

  protected handleError(error: Response | any) {
    const errorMsg = (typeof error.json === 'function' ? error.json().message : error.message || 'Server error');
    console.log(errorMsg)
    return observableThrowError(error.error);
  }


}
