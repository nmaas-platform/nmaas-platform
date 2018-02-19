import {Injectable} from '@angular/core';
import {AuthHttp} from 'angular2-jwt';
import {Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';
import {Observable} from 'rxjs/Observable';

import {AppConfigService} from './appconfig.service';

@Injectable()
export class GenericDataService {

  constructor(protected authHttp: AuthHttp, protected appConfig: AppConfigService) {}

  protected get<T>(url: string): Observable<T> {
    return this.authHttp.get(url)
      .timeout(this.appConfig.getHttpTimeout())
      .map(this.handleSuccess)
      .catch(this.handleError);
  }

  protected post<S, T>(url: string, entity: S): Observable<T> {
    return this.authHttp.post(url, entity)
      .timeout(this.appConfig.getHttpTimeout())
      .map(this.handleSuccess)
      .catch(this.handleError);
  }

  protected put<S, T>(url: string, entity: S): Observable<T> {
    return this.authHttp.put(url, entity)
      .timeout(this.appConfig.getHttpTimeout())
      .map(this.handleSuccess)
      .catch(this.handleError);
  }

  protected delete<T>(url: string): Observable<T> {
    return this.authHttp.delete(url)
      .timeout(this.appConfig.getHttpTimeout())
      .map(this.handleSuccess)
      .catch(this.handleError);
  }

  protected handleSuccess(res: Response) {
    return (res.arrayBuffer().byteLength > 0 ? res.json() : {});
  }

  protected handleError(error: Response | any) {
    const errorMsg = (typeof error.json === 'function' ? error.json().message : error.message || 'Server error');
    return Observable.throw(errorMsg);
  }


}
