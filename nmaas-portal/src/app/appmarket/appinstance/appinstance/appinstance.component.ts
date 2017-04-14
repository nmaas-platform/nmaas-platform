import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { Router, ActivatedRoute, Params }   from '@angular/router';
import { Location }                 from '@angular/common';

import { IntervalObservable } from 'rxjs/observable/IntervalObservable';
//import 'rxjs/add/operator/switchMap';

import { AppsService } from '../../../service/apps.service';
import { AppInstanceService } from '../../../service/appinstance.service';

import { AppInstanceProgressComponent } from '../appinstanceprogress/appinstanceprogress.component';

import { Application, AppInstance, AppInstanceState, AppInstanceStateAware, AppInstanceStatus, AppInstanceProgressStage } from '../../../model/index';

@Component({
    selector: 'nmaas-appinstance',
    templateUrl: './appinstance.component.html',
    styleUrls: ['./appinstance.component.css', '../../appdetails/appdetails.component.css'],
    providers: [AppsService, AppInstanceService]
})
@AppInstanceStateAware
export class AppInstanceComponent implements OnInit {

    @ViewChild(AppInstanceProgressComponent)
    public appInstanceProgress: AppInstanceProgressComponent;


    app: Application;

    private appInstanceStatus: AppInstanceStatus; // = new AppInstanceStatus();

    private appInstanceId: Number;
    private appInstance: AppInstance;

    private intervalCheckerSubscribtion;

    constructor(private appsService: AppsService, private appInstanceService: AppInstanceService, private router: Router, private route: ActivatedRoute, private location: Location) { }

    private configurationTemplate: string;
    
    ngOnInit() {
        this.route.params.subscribe(params => {
            this.appInstanceId = +params['id'];

            this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
                this.appInstance = appInstance;
                this.appsService.getApp(this.appInstance.applicationId).subscribe(app => { 
                    this.app = app;
                    this.configurationTemplate = this.app.configTemplate.template;
                });
            });



            this.intervalCheckerSubscribtion = IntervalObservable.create(3000).subscribe(
                () => {
                    console.debug('Tick: get app instance status');
                    this.appInstanceService.getAppInstanceState(this.appInstanceId).subscribe(
                        appInstanceStatus => {
                            console.log('Type: ' + typeof appInstanceStatus.state + ', ' + appInstanceStatus.state);
                            this.appInstanceStatus = appInstanceStatus;
                            this.appInstanceProgress.activeState = this.appInstanceStatus.state;
                            if(this.appInstanceStatus.state == AppInstanceState.RUNNING && !this.appInstance.url)
                                this.updateAppInstance();
                        }
                    )
                }
            );
        });
    }

    private updateAppInstance() {
        this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
                this.appInstance = appInstance;
            });
    }
    
    ngOnDestroy() {
        if (this.intervalCheckerSubscribtion)
            this.intervalCheckerSubscribtion.unsubscribe();
    }

    public applyConfiguration(configuration: string): void {
        this.appInstanceService.applyConfiguration(this.appInstanceId, configuration).subscribe(() => console.log('Configuration applied'));
    }
    
    public unsubscribe(): void {
        if (this.appInstanceId) {
            this.appInstanceService.removeAppInstance(this.appInstanceId).subscribe(() => this.router.navigate(['/']));
        }
    }

    private getStages(): AppInstanceProgressStage[] {
        return this.appInstanceService.getProgressStages();
    }

}
