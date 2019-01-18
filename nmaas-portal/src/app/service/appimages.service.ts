
import {throwError as observableThrowError,  Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import {FileInfo} from "../model";
import { AppConfigService } from '../service/appconfig.service';





@Injectable()
export class AppImagesService {

    constructor(private http: HttpClient, private appConfig: AppConfigService) { }

    public getAppLogoUrl(id: number):string {
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
            .catch((error: any) => observableThrowError(error.message || 'Server error'));
    }

}