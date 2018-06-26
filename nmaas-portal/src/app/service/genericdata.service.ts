import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';

import {AppConfigService} from './appconfig.service';

@Injectable()
export class GenericDataService {

  constructor(protected http: HttpClient, protected appConfig: AppConfigService) {}

  protected get<T>(url: string): Observable<T> {
    return this.http.get(url)
      .timeout(this.appConfig.getHttpTimeout())
      .catch(this.handleError);
  }

  protected post<S, T>(url: string, entity: S): Observable<T> {
    return this.http.post(url, entity)
      .timeout(this.appConfig.getHttpTimeout())
      .catch(this.handleError);
  }

  protected put<S, T>(url: string, entity: S): Observable<T> {
    return this.http.put(url, entity)
      .timeout(this.appConfig.getHttpTimeout())
      .catch(this.handleError);
  }

  protected delete<T>(url: string): Observable<T> {
    return this.http.delete(url)
      .timeout(this.appConfig.getHttpTimeout())
      .catch(this.handleError);
  }

  protected handleError(error: Response | any) {
    const errorMsg = (typeof error.json === 'function' ? error.json().message : error.message || 'Server error');
    return Observable.throw(errorMsg);
  }


}
