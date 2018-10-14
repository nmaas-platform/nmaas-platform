import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import {FileInfo} from "../model";
import { AppConfigService } from '../service/appconfig.service';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class AppImagesService {

    constructor(private http: HttpClient, private appConfig: AppConfigService) { }

    public getAppLogoUrl(id: number):string {
        console.debug('Getting logo: '+id);
        return this.appConfig.getApiUrl() + '/apps/' + id + '/logo';
    }
    
    public getAppScreenshotUrl(appId: number, screenshotId: number) {
        return this.appConfig.getApiUrl() + '/apps/' + appId + '/screenshots/' + screenshotId;
    }
    
    public getAppScreenshotsUrls(id: number): Observable<string[]> {
        return this.http.get<FileInfo>(this.appConfig.getApiUrl() + '/apps' + id + '/screenshots')
            .timeout(10000)
            .map(res=> {
                return res.id
            })
            .catch((error: any) => Observable.throw(error.message || 'Server error'));
    }

}