import {Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {IntervalObservable} from 'rxjs/observable/IntervalObservable';
import {AppImagesService, AppInstanceService, AppsService} from '../../../service/index';
import {AppInstanceProgressComponent} from '../appinstanceprogress/appinstanceprogress.component';
import {AppInstance, AppInstanceProgressStage, AppInstanceState, AppInstanceStatus, Application} from '../../../model/index';
import {SecurePipe} from '../../../pipe/index';
import {AppRestartModalComponent} from "../../modals/apprestart";
import {AppInstanceStateHistory} from "../../../model/appinstancestatehistory";
// import 'rxjs/add/operator/switchMap';
import {RateComponent} from '../../../shared/rate/rate.component';
import {AppConfiguration} from "../../../model/appconfiguration";
import {isNullOrUndefined} from "util";
import {SESSION_STORAGE, StorageService} from "ngx-webstorage-service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

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

  @ViewChild(RateComponent)
  public readonly appRate: RateComponent;

  app: Application;

  public appInstanceStatus: AppInstanceStatus;

  public appInstanceId: number;
  public appInstance: AppInstance;
  public appInstanceStateHistory: AppInstanceStateHistory[];
  public configurationTemplate: any;
  public appConfiguration: AppConfiguration;
  public requiredFields: any[];

  public intervalCheckerSubscribtion;

  public configAdvancedTab: FormGroup;

  jsonFormOptions: any = {
    addSubmit: false, // Add a submit button if layout does not have one
    debug: false, // Don't show inline debugging information
    loadExternalAssets: false, // Load external css and JavaScript for frameworks
    returnEmptyFields: false, // Don't return values for empty input fields
    setSchemaDefaults: true, // Always use schema defaults for empty fields
    defaultWidgetOptions: { feedback: false }, // Show inline feedback icons
    options: {},
    widgetOptions: {}
  };

  constructor(private appsService: AppsService,
    public appImagesService: AppImagesService,
    private appInstanceService: AppInstanceService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    @Inject(SESSION_STORAGE) private storage: StorageService,
              private fb: FormBuilder) {
      this.configAdvancedTab = fb.group({
      storageSpace: ['', [Validators.min(1), Validators.pattern('^[0-9]*$')]]
    });
  }

  ngOnInit() {
    this.appConfiguration = new AppConfiguration();
    this.route.params.subscribe(params => {
      this.appInstanceId = +params['id'];

      this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
        this.appInstance = appInstance;
        this.appsService.getApp(this.appInstance.applicationId).subscribe(app => {
          this.app = app;
          this.configurationTemplate = this.getTemplate(this.app.configTemplate.template);
          this.requiredFields = this.configurationTemplate.schema.required;
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
        this.appInstanceProgress.previousState = this.appInstanceStatus.previousState;
        if (AppInstanceState[AppInstanceState[this.appInstanceStatus.state]] === AppInstanceState[AppInstanceState.RUNNING] && !this.appInstance.url) {
          this.updateAppInstance();
        }
      }
    );
     this.appInstanceService.getAppInstanceHistory(this.appInstanceId).subscribe(history => {
        this.appInstanceStateHistory = history.reverse();
     });
  }

  private updateAppInstance() {
    console.log('update app instance');
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

  public redeploy(): void{
    this.appInstanceService.redeployAppInstance(this.appInstanceId).subscribe(() => console.log("Redeployed"));
  }

  public changeConfiguration(configuration: string): void{
    this.appConfiguration.jsonInput = configuration;
  }

  public applyConfiguration(): void {
    if(this.isValid()){
      this.appConfiguration.storageSpace = this.configAdvancedTab.controls['storageSpace'].value;
        this.appInstanceService.applyConfiguration(this.appInstanceId, this.appConfiguration).subscribe(() => {
          console.log(this.appConfiguration.storageSpace)
          console.log('Configuration applied');
          this.storage.set("appConfig_"+this.appInstanceId.toString(), this.appConfiguration);
        });
    }
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

  public onRateChanged(): void {
        this.appRate.refresh();
  }

  private isValid(): boolean {
    if(isNullOrUndefined(this.requiredFields)){
      return true;
    }
    for(let value of this.requiredFields){
      if(!this.appConfiguration.jsonInput.hasOwnProperty(value)){
          return false;
      } else if(!isNullOrUndefined(this.configurationTemplate.schema.properties[value].items) && this.configurationTemplate.schema.properties[value].items.properties.hasOwnProperty("ipAddress")){
        for(let val of this.appConfiguration.jsonInput[value]){
            if(!val.ipAddress.match(this.configurationTemplate.schema.properties[value].items.properties.ipAddress["pattern"])){
                return false;
            }
        }
      }
    }
    return true;
  }

}
