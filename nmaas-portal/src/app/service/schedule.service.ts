import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {JobDescriptor} from "../model/jobdescriptor";
import {Observable} from "rxjs";

@Injectable()
export class ScheduleService extends GenericDataService{

  private readonly url: string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/schedule/'
  }

  public createJob(jobDescriptor: JobDescriptor):Observable<any>{
    return this.post(this.url, jobDescriptor);
  }

  public executeJob(name: string):Observable<any>{
    return this.http.post(this.url + name + '/execute', null);
  }

  public updateJob(jobDescriptor: JobDescriptor): Observable<any>{
    return this.put(this.url, jobDescriptor);
  }

  public deleteJob(name: string): Observable<any>{
    return this.delete(this.url + name);
  }

  public deleteAllJobs(): Observable<any>{
    return this.delete(this.url + 'all');
  }

  public resumeJob(name: string): Observable<any>{
    return this.http.patch(this.url + name + '/resume', null);
  }

  public pauseJob(name: string): Observable<any> {
     return this.http.patch(this.url + name + '/pause', null);
  }

}
