
import {throwError as observableThrowError,  Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import {FileInfo} from "../model";
import { AppConfigService } from '../service/appconfig.service';
import {catchError, debounceTime, map} from 'rxjs/operators';
import {GenericDataService} from "./genericdata.service";

@Injectable()
export class AppImagesService extends GenericDataService {

    constructor(public http: HttpClient, public appConfig: AppConfigService) {
        super(http, appConfig);
    }

    public getAppLogoUrl(id: number):string {
        return this.appConfig.getApiUrl() + '/apps/' + id + '/logo';
    }

    public getLogoFile(id: number): any {
        return this.http.get(this.appConfig.getApiUrl() + '/apps/' + id + '/logo', {responseType: "blob"});
    }
    
    public getAppScreenshotUrl(appId: number, screenshotId: number) {
        return this.appConfig.getApiUrl() + '/apps/' + appId + '/screenshots/' + screenshotId;
    }

    public getAppScreenshotFile(appId:number, id: number): any {
        return this.http.get(this.appConfig.getApiUrl() + '/apps/' + appId + '/screenshots/' + id, {responseType: "blob"});
    }
    
    public getAppScreenshotsUrls(id: number): Observable<FileInfo[]> {
        return this.http.get<FileInfo[]>(this.appConfig.getApiUrl() + '/apps/' + id + '/screenshots').pipe(
            debounceTime(10000),
            catchError((error: any) => observableThrowError(error.message || 'Server error')));
    }

    public deleteScreenshots(id: number): Observable<any> {
        return this.delete(this.appConfig.getApiUrl() + '/apps/' + id + '/screenshots/all');
    }

    public deleteLogo(id: number): Observable<any> {
        return this.delete(this.appConfig.getApiUrl() + '/apps/' + id + '/logo');
    }

}
