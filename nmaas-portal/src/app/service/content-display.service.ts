import { Injectable } from '@angular/core';
import {GenericDataService} from './genericdata.service';
import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';
import {Content} from '../model/content';
import {Observable} from 'rxjs/internal/Observable';

@Injectable({
    providedIn: 'root',
})
export class ContentDisplayService extends GenericDataService {

    constructor(http: HttpClient, appConfig: AppConfigService) {
        super(http, appConfig);
    }

    public getContent(name: string): Observable<Content> {
        return this.http.get<Content>(this.getContentUrl() + name);
    }

    protected getContentUrl(): string {
        return this.appConfig.getApiUrl() + '/content/'
    }

}
