import { Injectable } from '@angular/core';
import { AuthHttp } from 'angular2-jwt';
import { Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';

import { AppConfigService } from '../service/appconfig.service';

import { Id } from '../model/id';
import { AppInstanceStatus } from '../model/appinstancestatus';
import { AppInstance } from '../model/appinstance';
import { AppInstanceState } from '../model/appinstancestatus';
import { AppInstanceProgressStage } from '../model/appinstanceprogressstage';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class AppInstanceService {
    static APPINSTANCE_URL: string = '/apps/instances/';

    constructor(private authHttp: AuthHttp, private appConfig: AppConfigService) { }

    public getAllAppInstances(): Observable<AppInstance[]> {
        return this.authHttp.get(this.getUrl())
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public getMyAppInstances(): Observable<AppInstance[]> {
        return this.authHttp.get(this.getUrl()+'my')
            .timeout(10000)
            .map((res: Response) => res.json() as AppInstance[])
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));        
    }
    
    public getUserAppInstances(username: string): Observable<AppInstance[]> {
        return this.authHttp.get(this.getUrl()+'user/' + username)
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));        
    } 
    
    public getAppInstanceState(id: Number): Observable<AppInstanceStatus> {
        return this.authHttp.get(this.getUrl() + id + '/state')
            .timeout(10000)
            .map((res: Response) => { 
                var ais = res.json(); 
                return new AppInstanceStatus(ais.appInstanceId, <AppInstanceState>(AppInstanceState[<string>(ais.state)]), ais.details); 
            })
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public createAppInstance(appId: Number, name: string): Observable<Id> {
        return this.authHttp.post(this.getUrl(), JSON.stringify({ "applicationId": appId, "name": name }))
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public removeAppInstance(appInstanceId: Number): Observable<void> {
        return this.authHttp.delete(this.getUrl() + appInstanceId)
            .timeout(10000)
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public getAppInstance(appInstanceId: Number): Observable<AppInstance> {
        return this.authHttp.get(this.getUrl() + appInstanceId)
            .timeout(10000)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));
    }

    public applyConfiguration(appInstanceId: Number, configuration): Observable<void> {
        return this.authHttp.post(this.getUrl() + appInstanceId + '/configure', configuration)
            .timeout(10000)
            .catch((error: any) => Observable.throw(error.json().message || 'Server error'));        
    }
    
    private getUrl(): string {
        return this.appConfig.getApiUrl() + AppInstanceService.APPINSTANCE_URL;
    }

    public getProgressStages(): AppInstanceProgressStage[] {
        return [
            new AppInstanceProgressStage('Subscription validation', AppInstanceState.VALIDATION),
            new AppInstanceProgressStage('Environment creation', AppInstanceState.PREPARATION),
            new AppInstanceProgressStage('Setting up connectivity', AppInstanceState.CONNECTING),
            new AppInstanceProgressStage('Providing onfiguration', AppInstanceState.CONFIGURATION_AWAITING),
            new AppInstanceProgressStage('App container deployment', AppInstanceState.DEPLOYING),
            new AppInstanceProgressStage('App running', AppInstanceState.RUNNING),
            new AppInstanceProgressStage('Undeploying', AppInstanceState.UNDEPLOYING, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE]),
            new AppInstanceProgressStage('Done', AppInstanceState.DONE, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE])
        ];
    }

}
