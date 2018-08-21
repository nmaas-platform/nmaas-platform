import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "../../../node_modules/@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "../../../node_modules/rxjs/Observable";
import {Content} from "../model/content";

@Injectable()
export class ContentDisplayService extends GenericDataService {

    constructor(http: HttpClient, appConfig: AppConfigService) {
        super(http, appConfig);
    }

    public getContent(name: String): Observable<Content>{
        return this.http.get<Content>(this.getProfileUrl()+name.toString());
    }

    protected getProfileUrl(): string{
        return this.appConfig.getApiUrl()+'/content/'
    }
}
