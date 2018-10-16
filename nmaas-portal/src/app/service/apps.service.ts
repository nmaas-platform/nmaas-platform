import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Id } from '../model/id';
import { Application } from '../model/application';
import { Rate } from '../model/rate';
import { Comment } from '../model/comment';
import { FileInfo } from '../model/fileinfo';
import { AppConfigService } from '../service/appconfig.service';
import { GenericDataService } from './genericdata.service';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class AppsService extends GenericDataService {

    constructor(http: HttpClient, appConfig: AppConfigService) {
      super(http, appConfig);
    }


    public getApps(): Observable<Application[]> {
        return this.get<Application[]>(this.appConfig.getApiUrl() + '/apps');
    }

    public getApp(id: number): Observable<Application> {
        return this.get<Application>(this.appConfig.getApiUrl() + '/apps/' + id);
    }

    public getAppRateByUrl(urlPath: string): Observable<Rate> {
        return this.getByUrl(urlPath);
    }

    public setMyAppRateByUrl(urlPath: string): Observable<any> {
        return this.http.post(this.appConfig.getApiUrl() + urlPath, null)
            .timeout(10000)
            .catch((error: any) => Observable.throw((typeof error.json === 'function' ? error.json().message : 'Server error')));
    }

    public getAppCommentsByUrl(urlPath: string): Observable<Comment[]> {
        return this.getByUrl(urlPath);
    }

    public addAppCommentByUrl(urlPath: string, comment: Comment): Observable<Id> {
        return this.http.post(this.appConfig.getApiUrl() + urlPath, comment)
            .timeout(10000)
            .catch((error: any) => Observable.throw((typeof error.json === 'function' ? error.json().message : 'Server error')));
    }

    public deleteAppCommentByUrl(urlPath: string, id: Id): Observable<any> {
        return this.http.delete(this.appConfig.getApiUrl() + urlPath + '/' + id.id)
            .timeout(10000)            
            .catch((error: any) => Observable.throw((typeof error.json === 'function' ? error.json().message : 'Server error')));
    }
    
    public getAppScreenshotsByUrl(urlPath: string): Observable<FileInfo[]> {
        return this.getByUrl(urlPath);
    }

    public getAppScreenshotUrl(urlPath: string): string {
        return this.appConfig.getApiUrl() + urlPath;
    }

    private getByUrl(urlPath: string): Observable<any>{
        return this.http.get(this.appConfig.getApiUrl() + urlPath)
            .timeout(10000)
            .catch((error: any) => Observable.throw((typeof error.json === 'function' ? error.json().message : 'Server error')));
    }
}