import {AfterViewChecked, Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {IntervalObservable} from 'rxjs/observable/IntervalObservable';
import {AppImagesService, AppInstanceService, AppsService} from '../../../service/index';
import {AppInstanceProgressComponent} from '../appinstanceprogress/appinstanceprogress.component';
import {
  AppInstance,
  AppInstanceProgressStage,
  AppInstanceState,
  AppInstanceStatus,
  Application
} from '../../../model/index';
import {SecurePipe} from '../../../pipe/index';
import {AppRestartModalComponent} from "../../modals/apprestart";
import {AppInstanceStateHistory} from "../../../model/appinstancestatehistory";
// import 'rxjs/add/operator/switchMap';
import {RateComponent} from '../../../shared/rate/rate.component';
import {AppConfiguration} from "../../../model/appconfiguration";
import {isNullOrUndefined} from "util";
import {LOCAL_STORAGE, StorageService} from "ngx-webstorage-service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ModalComponent} from "../../../shared/modal";

@Component({
  selector: 'nmaas-appinstance',
  templateUrl: './appinstance.component.html',
  styleUrls: ['./appinstance.component.css', '../../appdetails/appdetails.component.css'],
  providers: [AppsService, AppImagesService, AppInstanceService, SecurePipe, AppRestartModalComponent]
})
export class AppInstanceComponent implements OnInit, OnDestroy, AfterViewChecked {

  public AppInstanceState = AppInstanceState;

  @ViewChild(AppInstanceProgressComponent)
  public appInstanceProgress: AppInstanceProgressComponent;

  @ViewChild(AppRestartModalComponent)
  public modal:AppRestartModalComponent;

  @ViewChild(ModalComponent)
  public undeployModal: ModalComponent;

  @ViewChild('updateConfig')
  public updateConfigModal: ModalComponent;

  @ViewChild(RateComponent)
  public readonly appRate: RateComponent;

  app: Application;

  public appInstanceStatus: AppInstanceStatus;

  public appInstanceId: number;
  public appInstance: AppInstance;
  public appInstanceStateHistory: AppInstanceStateHistory[];
  public configurationTemplate: any;
  public additionalParametersTemplate: any;
  public additionalMandatoryTemplate: any;
  public appConfiguration: AppConfiguration;

  public intervalCheckerSubscribtion;

  public configAdvancedTab: FormGroup;

  constructor(private appsService: AppsService,
    public appImagesService: AppImagesService,
    private appInstanceService: AppInstanceService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    @Inject(LOCAL_STORAGE) public storage: StorageService,
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
          if(!isNullOrUndefined(this.app.additionalParametersTemplate)){
              this.additionalParametersTemplate = this.getTemplate(this.app.additionalParametersTemplate.template);
          }
          if(!isNullOrUndefined(this.app.additionalMandatoryTemplate) && !isNullOrUndefined(this.app.additionalMandatoryTemplate.template)){
            this.additionalMandatoryTemplate = this.getTemplate(this.app.additionalMandatoryTemplate.template);
          }
        });
      });

      this.updateAppInstanceState();
      this.intervalCheckerSubscribtion = IntervalObservable.create(5000).subscribe(() => this.updateAppInstanceState());
      this.undeployModal.setModalType("warning");
      this.undeployModal.setStatusOfIcons(true);
    });
  }

  ngAfterViewChecked(): void {
  }

  private updateAppInstanceState() {
    this.appInstanceService.getAppInstanceState(this.appInstanceId).subscribe(
      appInstanceStatus => {
        console.log('Type: ' + typeof appInstanceStatus.state + ', ' + appInstanceStatus.state);
        this.appInstanceStatus = appInstanceStatus;
        if(this.appInstanceStatus.state == this.AppInstanceState.FAILURE){
          document.getElementById("app-prop").scrollLeft =
            (document.getElementsByClassName("stepwizard-btn-success").length * 180 +
              document.getElementsByClassName("stepwizard-btn-danger").length * 180);
        }
        this.appInstanceProgress.activeState = this.appInstanceStatus.state;
        this.appInstanceProgress.previousState = this.appInstanceStatus.previousState;
        document.getElementById("app-prop").scrollLeft =
          (document.getElementsByClassName("stepwizard-btn-success").length * 180 +
            document.getElementsByClassName("stepwizard-btn-danger").length * 180);
        if (AppInstanceState[AppInstanceState[this.appInstanceStatus.state]] === AppInstanceState[AppInstanceState.RUNNING]) {
          if(this.storage.has("appConfig_"+this.appInstanceId.toString()))
            this.storage.remove("appConfig_"+this.appInstanceId.toString());
          if(!this.appInstance.url)
            this.updateAppInstance();
        }
      }
    );
     this.appInstanceService.getAppInstanceHistory(this.appInstanceId).subscribe(history => {
        this.appInstanceStateHistory = [...history].reverse();
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

  public changeAdditionalParameters(additionalParameters: any): void{
    if(!isNullOrUndefined(additionalParameters)){
      this.appConfiguration.additionalParameters = additionalParameters;
    }
  }

  public changeMandatoryParameters(mandatoryParameters: any): void{
    if(!isNullOrUndefined(mandatoryParameters)){
      this.appConfiguration.mandatoryParameters = mandatoryParameters;
    }
  }

  public applyConfiguration(input:any): void {
    this.appConfiguration.storageSpace = this.configAdvancedTab.controls['storageSpace'].value;
    this.appConfiguration.jsonInput = input;
    this.appInstanceService.applyConfiguration(this.appInstanceId, this.appConfiguration).subscribe(() => {
      console.log('Configuration applied');
      this.storage.set("appConfig_"+this.appInstanceId.toString(), this.appConfiguration);
    });
  }

  public updateConfiguration(): void {
      this.appInstanceService.updateConfiguration(this.appInstanceId, this.appConfiguration).subscribe(() => {
        console.log("Configuration updated");
        this.updateConfigModal.hide();
      });
  }

  public undeploy(): void {
    if (this.appInstanceId) {
      this.appInstanceService.removeAppInstance(this.appInstanceId).subscribe(() => this.router.navigate(['/instances']));
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

}
