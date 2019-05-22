import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {AppConfigService} from "./appconfig.service";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {MonitorEntry} from "../model/monitorentry";
import {Observable} from "rxjs";
import {timeout} from "rxjs/operators";
import {any} from "codelyzer/util/function";
import {reject} from "q";

@Injectable()
export class MonitorService extends GenericDataService{

  private readonly url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/monitor/'
  }

  public updateMonitorEntryAndJob(monitorEntry: MonitorEntry):Observable<any>{
    return this.put(this.url, monitorEntry);
  }

  public getAllMonitorEntries():Observable<MonitorEntry[]>{
    return this.get(this.url + 'all');
  }

  public getOneMonitorEntry(serviceName: string):Observable<MonitorEntry>{
    return this.get(this.url + serviceName);
  }

  public executeJob(serviceName: string):Observable<any>{
    return this.http.post(this.url + serviceName + '/execute', null);
  }

  public resumeJob(serviceName: string): Observable<any>{
    return this.http.patch(this.url + serviceName + '/resume', null);
  }

  public pauseJob(serviceName: string): Observable<any> {
    return this.http.patch(this.url + serviceName + '/pause', null);
  }

}
