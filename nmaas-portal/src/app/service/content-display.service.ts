import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "../../../node_modules/@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "../../../node_modules/rxjs/Observable";
import {Content} from "../model/content";

@Injectable()
export class ContentDisplayService extends GenericDataService {

    private updateRequiredFlag: boolean = false;

    constructor(http: HttpClient, appConfig: AppConfigService) {
        super(http, appConfig);
    }

    public getContent(name: string): Observable<Content>{
        return this.http.get<Content>(this.getContentUrl()+name);
    }

    public getLanguages(): Observable<string[]>{
        return this.get(this.getContentUrl() + 'languages');
    }

    protected getContentUrl(): string{
        return this.appConfig.getApiUrl()+'/content/'
    }

    public setUpdateRequiredFlag(flag:boolean){
        this.updateRequiredFlag = flag;
    }

    public shouldUpdate(): boolean{
        return this.updateRequiredFlag;
    }
}
