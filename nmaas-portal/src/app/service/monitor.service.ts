import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {AppConfigService} from "./appconfig.service";
import {HttpClient} from "@angular/common/http";
import {MonitorEntry} from "../model/monitorentry";
import {Observable} from "rxjs";

@Injectable()
export class MonitorService extends GenericDataService{

  private readonly url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/monitor/'
  }

  public createMonitorEntry(monitorEntry: MonitorEntry):Observable<any>{
    return this.post(this.url, monitorEntry);
  }

  public updateMonitorEntry(monitorEntry: MonitorEntry):Observable<any>{
    return this.put(this.url, monitorEntry);
  }

  public deleteMonitorEntry(serviceName: string):Observable<any>{
    return this.delete(this.url + serviceName);
  }

  public getAllMonitorEntries():Observable<MonitorEntry[]>{
    return this.get(this.url + 'all');
  }

  public getOneMonitorEntry(serviceName: string):Observable<MonitorEntry>{
    return this.get(this.url + serviceName);
  }

}
