import { Injectable } from '@angular/core';
import { AuthHttp } from 'angular2-jwt';
import { Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';

import { FileInfo } from '../model/fileinfo';
import { AppConfigService } from '../service/appconfig.service';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class AppImagesService {

    constructor(private http: Http, private appConfig: AppConfigService) { }

    public getAppLogoUrl(id: Number):string {
        return this.appConfig.getApiUrl() + '/apps/' + id + '/logo';
    }
    
    public getAppScreenshotUrl(appId: number, screenshotId: number) {
        return this.appConfig.getApiUrl() + '/apps/' + appId + '/screenshots/' + screenshotId;
    }
    
    public getAppScreenshotsUrls(id: Number): Observable<string[]> {
        return this.http.get(this.appConfig.getApiUrl() + '/apps' + id + '/screenshots')
            .timeout(10000)
            .map((res: Response) => (res.json() as FileInfo).id)
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

}