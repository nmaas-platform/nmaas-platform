import {AfterViewChecked, Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
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
import {RateComponent} from '../../../shared/rate/rate.component';
import {AppConfiguration} from "../../../model/appconfiguration";
import {isNullOrUndefined} from "util";
import {LOCAL_STORAGE, StorageService} from "ngx-webstorage-service";
import {ModalComponent} from "../../../shared/modal";
import {interval} from 'rxjs/internal/observable/interval';

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
  public configurationUpdateTemplate:any;
  public submission: any = { data:{} };
  public appConfiguration: AppConfiguration;

  public intervalCheckerSubscribtion;

  constructor(private appsService: AppsService,
    public appImagesService: AppImagesService,
    private appInstanceService: AppInstanceService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    @Inject(LOCAL_STORAGE) public storage: StorageService) {}

  ngOnInit() {
    this.appConfiguration = new AppConfiguration();
    this.route.params.subscribe(params => {
      this.appInstanceId = +params['id'];

      this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
        this.appInstance = appInstance;
        this.configurationTemplate = this.getTemplate(appInstance.configTemplate.template);
        this.submission.data.configuration = JSON.parse(appInstance.configuration);
        this.appsService.getApp(this.appInstance.applicationId).subscribe(app => {
          this.app = app;
          if(!isNullOrUndefined(this.app.configurationUpdateTemplate)){
              this.configurationUpdateTemplate = this.getTemplate(this.app.configurationUpdateTemplate.template);
          }
        });
      });

      this.updateAppInstanceState();
      this.intervalCheckerSubscribtion = interval(5000).subscribe(() => this.updateAppInstanceState());
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
      this.submission.data.configuration = JSON.parse(appInstance.configuration);
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

  public changeAccessCredentials(accessCredentials: any): void{
      if(!isNullOrUndefined(accessCredentials)){
          this.appConfiguration.accessCredentials = accessCredentials;
      }
  }

  public changeConfiguration(configuration: any): void{
    if(!isNullOrUndefined(configuration)){
      this.appConfiguration.jsonInput = configuration;
    }
  }

  public applyConfiguration(input:any): void {
    if(!isNullOrUndefined(input['advanced'])){
      this.appConfiguration.storageSpace = input['advanced'].storageSpace;
    }
    this.changeMandatoryParameters(input['mandatoryParameters']);
    this.changeAdditionalParameters(input['additionalParameters']);
    this.changeConfiguration(input['configuration']);
    this.changeAccessCredentials(input['accessCredentials']);
    if(isNullOrUndefined(this.appConfiguration.jsonInput)){
        this.appConfiguration.jsonInput = {};
    }
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

  public changeConfigUpdate(input): void {
    if(!isNullOrUndefined(input)){
      this.changeConfiguration(input['configuration']);
      this.changeAccessCredentials(input['accessCredentials']);
    }
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
