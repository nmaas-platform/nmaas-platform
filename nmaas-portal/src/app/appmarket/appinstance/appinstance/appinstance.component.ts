import {Component, OnInit, OnDestroy, Input, ViewChild} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';

import {IntervalObservable} from 'rxjs/observable/IntervalObservable';
// import 'rxjs/add/operator/switchMap';

import {AppsService, AppInstanceService, AppImagesService} from '../../../service/index';

import {AppInstanceProgressComponent} from '../appinstanceprogress/appinstanceprogress.component';
import {RateComponent} from '../../../shared/rate/rate.component';

import {
  Application,
  AppInstance,
  AppInstanceState,
  AppInstanceStatus,
  AppInstanceProgressStage
} from '../../../model/index';

import {SecurePipe} from '../../../pipe/index';
import { isNullOrUndefined } from 'util';
import {AppRestartModalComponent} from "../../modals/apprestart";


@Component({
  selector: 'nmaas-appinstance',
  templateUrl: './appinstance.component.html',
  styleUrls: ['./appinstance.component.css', '../../appdetails/appdetails.component.css'],
  providers: [AppsService, AppImagesService, AppInstanceService, SecurePipe, AppRestartModalComponent]
})
export class AppInstanceComponent implements OnInit, OnDestroy {

  public AppInstanceState = AppInstanceState;

  @ViewChild(AppInstanceProgressComponent)
  public appInstanceProgress: AppInstanceProgressComponent;

  @ViewChild(AppRestartModalComponent)
  public modal:AppRestartModalComponent;

  app: Application;

  public appInstanceStatus: AppInstanceStatus;

  public appInstanceId: number;
  public appInstance: AppInstance;
  public configurationTemplate: any;

  public intervalCheckerSubscribtion;

  jsonFormOptions: any = {
    addSubmit: false, // Add a submit button if layout does not have one
    debug: false, // Don't show inline debugging information
    loadExternalAssets: false, // Load external css and JavaScript for frameworks
    returnEmptyFields: false, // Don't return values for empty input fields
    setSchemaDefaults: true, // Always use schema defaults for empty fields
    defautWidgetOptions: { feedback: false }, // Show inline feedback icons
    options: {},
    widgetOptions: {}
  };

  constructor(private appsService: AppsService,
    public appImagesService: AppImagesService,
    private appInstanceService: AppInstanceService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.appInstanceId = +params['id'];

      this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
        this.appInstance = appInstance;
        this.appsService.getApp(this.appInstance.applicationId).subscribe(app => {
          this.app = app;
          this.configurationTemplate = this.getTemplate(this.app.configTemplate.template);
        });
      });


      this.updateAppInstanceState();
      this.intervalCheckerSubscribtion = IntervalObservable.create(5000).subscribe(() => this.updateAppInstanceState());
    });
  }

  private updateAppInstanceState() {
    this.appInstanceService.getAppInstanceState(this.appInstanceId).subscribe(
      appInstanceStatus => {
        console.log('Type: ' + typeof appInstanceStatus.state + ', ' + appInstanceStatus.state);
        this.appInstanceStatus = appInstanceStatus;
        this.appInstanceProgress.activeState = this.appInstanceStatus.state;
        if (AppInstanceState[AppInstanceState[this.appInstanceStatus.state]] === AppInstanceState[AppInstanceState.RUNNING] && !this.appInstance.url) {
          this.updateAppInstance();
        }
      }
    )

  }

  private updateAppInstance() {
    console.log('update app instance')
    this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
      console.log('updated app instance url: ' + appInstance.url);
      this.appInstance = appInstance;
    });
  }

  ngOnDestroy() {
    if (this.intervalCheckerSubscribtion) {
      this.intervalCheckerSubscribtion.unsubscribe();
    }
  }

  public applyConfiguration(configuration: string): void {
    this.appInstanceService.applyConfiguration(this.appInstanceId, configuration).subscribe(() => console.log('Configuration applied'));
  }

  public undeploy(): void {
    if (this.appInstanceId) {
      this.appInstanceService.removeAppInstance(this.appInstanceId).subscribe(() => this.router.navigate(['/']));
    }
  }

  public getStages(): AppInstanceProgressStage[] {
    return this.appInstanceService.getProgressStages();
  }

  protected getTemplate(template: string): any {
    return template;
  }

  private getUserFriendlyString(stringToChange:string){
    let re = /_/gi;
    return stringToChange.charAt(0).toUpperCase() + stringToChange.substr(1,stringToChange.length).replace(re," ").toLowerCase();
  }

}
