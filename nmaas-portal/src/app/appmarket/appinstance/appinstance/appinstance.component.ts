import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { Router, ActivatedRoute, Params }   from '@angular/router';
import { Location }                 from '@angular/common';

import { IntervalObservable } from 'rxjs/observable/IntervalObservable';
//import 'rxjs/add/operator/switchMap';

import { AppsService } from '../../../service/apps.service';
import { AppInstanceService } from '../../../service/appinstance.service';

import { AppInstanceProgressComponent } from '../appinstanceprogress/appinstanceprogress.component';

import { Application, AppInstance, AppInstanceState, AppInstanceStatus, AppInstanceProgressStage } from '../../../model/index';

@Component({
    selector: 'nmaas-appinstance',
    templateUrl: './appinstance.component.html',
    styleUrls: ['./appinstance.component.css', '../../appdetails/appdetails.component.css'],
    providers: [AppsService, AppInstanceService]
})
export class AppInstanceComponent implements OnInit {

    @ViewChild(AppInstanceProgressComponent)
    public appInstanceProgress: AppInstanceProgressComponent;


    app: Application;

    private appInstanceStatus: AppInstanceStatus;

    private appInstanceId: Number;
    private appInstance: AppInstance;

    private intervalCheckerSubscribtion;

    constructor(private appsService: AppsService, private appInstanceService: AppInstanceService, private router: Router, private route: ActivatedRoute, private location: Location) { }

    ngOnInit() {
        this.route.params.subscribe(params => {
            this.appInstanceId = +params['id'];

            this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
                this.appInstance = appInstance;
                this.appsService.getApp(this.appInstance.applicationId).subscribe(app => this.app = app);
            });



            this.intervalCheckerSubscribtion = IntervalObservable.create(5000).subscribe(
                () => {
                    console.debug('Tick: get app instance status');
                    this.appInstanceService.getAppInstanceState(this.appInstanceId).subscribe(
                        appInstanceStatus => {
                            this.appInstanceStatus = appInstanceStatus;
                            //this.appInstanceProgress.setState(this.appInstanceStatus.state);
                            this.appInstanceProgress.activeState = this.appInstanceStatus.state;
                        }
                    )
                }
            );
        });
    }

    ngOnDestroy() {
        if (this.intervalCheckerSubscribtion)
            this.intervalCheckerSubscribtion.unsubscribe();
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
