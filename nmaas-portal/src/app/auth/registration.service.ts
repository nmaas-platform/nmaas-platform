import { Domain } from '../model/domain';
import { Registration } from '../model/registration';
import { AppConfigService } from '../service/appconfig.service';
import { JsonMapperService } from '../service/jsonmapper.service';
import { Injectable } from '@angular/core';
import {Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class RegistrationService {

  static headers = new Headers({'Content-Type': 'application/json', 'Accept': 'application/json'});
  
  constructor(private http: Http, private appConfig: AppConfigService, private jsonModelMapper: JsonMapperService) {}
  
  public register(registration: Registration): Observable<any> {
    return this.http.post(this.getUrl(), registration, new RequestOptions({headers: RegistrationService.headers}))
      .timeout(this.appConfig.getHttpTimeout())
      .map(this.handleSuccess)
      .catch(this.handleError);
  }

  public getDomains(): Observable<Domain[]> {
    return this.http.get(this.getUrl() + '/domains')
      .timeout(this.appConfig.getHttpTimeout())
      .map(this.handleSuccess)
      .catch(this.handleError)
      .map((domains) => this.jsonModelMapper.deserialize(domains, Domain));
  }

  protected handleSuccess(res: Response) {
    return (res.arrayBuffer().byteLength > 0 ? res.json() : {});
  }

  protected handleError(error: Response | any) {
    const errorMsg = (typeof error.json === 'function' ? error.json().message : error.message || 'Server error');
    return Observable.throw(errorMsg);
  }

  protected getUrl(): string {
    return this.appConfig.getApiUrl() + '/auth/basic/registration';
  }
  
}
