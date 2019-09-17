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

  private createProperUrl(): string{
    return (this.appConfig.getApiUrl() + '/monitor/');
  }

  public updateMonitorEntryAndJob(monitorEntry: MonitorEntry):Observable<any>{
    return this.put(this.createProperUrl(), monitorEntry);
  }

  public getAllMonitorEntries():Observable<MonitorEntry[]>{
      return this.get(this.createProperUrl() + 'all');
  }

  public getOneMonitorEntry(serviceName: string):Observable<MonitorEntry>{
    return this.get(this.createProperUrl() + serviceName);
  }

  public executeJob(serviceName: string):Observable<any>{
    return this.http.post(this.createProperUrl() + serviceName + '/execute', null);
  }

  public resumeJob(serviceName: string): Observable<any>{
    return this.http.patch(this.createProperUrl() + serviceName + '/resume', null);
  }

  public pauseJob(serviceName: string): Observable<any> {
    return this.http.patch(this.createProperUrl() + serviceName + '/pause', null);
  }

}
