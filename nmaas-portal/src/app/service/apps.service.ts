import { Injectable } from '@angular/core';
import { AuthHttp } from 'angular2-jwt';
import { Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';

import { Application } from '../model/application';
import { Rate } from '../model/rate';
import { Comment } from '../model/comment';
import { FileInfo } from '../model/fileinfo';
import { AppConfigService } from '../service/appconfig.service';


import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class AppsService {

    constructor(private authHttp: AuthHttp, private appConfig: AppConfigService) { }


    public getApps(): Observable<Application[]> {
        return this.authHttp.get(this.appConfig.getApiUrl() + '/apps')
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public getApp(id: Number): Observable<Application> {
        console.debug('Get app with id=' + id);
        return this.authHttp.get(this.appConfig.getApiUrl() + '/apps/' + id)
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public getAppRateByUrl(urlPath: string): Observable<Rate> {
        return this.authHttp.get(this.appConfig.getApiUrl() + urlPath)
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public getAppCommentsByUrl(urlPath: string): Observable<Comment[]> {
        return this.authHttp.get(this.appConfig.getApiUrl() + urlPath)
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public getAppScreenshotsByUrl(urlPath: string): Observable<FileInfo[]> {
        return this.authHttp.get(this.appConfig.getApiUrl() + urlPath)
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

}