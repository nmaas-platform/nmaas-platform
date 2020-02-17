import {AfterViewChecked, Component, EventEmitter, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {AppImagesService, AppInstanceService, AppsService, DomainService} from '../../../service';
import {AppInstanceProgressComponent} from '../appinstanceprogress';
import {AppInstance, AppInstanceProgressStage, AppInstanceState, AppInstanceStatus, Application} from '../../../model';
import {SecurePipe} from '../../../pipe';
import {AppRestartModalComponent} from '../../modals/apprestart';
import {AppInstanceStateHistory} from '../../../model/appinstancestatehistory';
import {RateComponent} from '../../../shared/rate';
import {AppConfiguration} from '../../../model/appconfiguration';
import {isNullOrUndefined} from 'util';
import {LOCAL_STORAGE, StorageService} from 'ngx-webstorage-service';
import {ModalComponent} from '../../../shared/modal';
import {interval} from 'rxjs/internal/observable/interval';
import {UserDataService} from '../../../service/userdata.service';
import {TranslateStateModule} from '../../../shared/translate-state/translate-state.module';
import {TranslateService} from '@ngx-translate/core';
import {SessionService} from '../../../service/session.service';
import {LocalDatePipe} from '../../../pipe/local-date.pipe';
import {ApplicationState} from '../../../model/applicationstate';
import {ServiceAccessMethod, ServiceAccessMethodType} from "../../../model/serviceaccessmethod";

@Component({
  selector: 'nmaas-appinstance',
  templateUrl: './appinstance.component.html',
  styleUrls: ['./appinstance.component.css', '../../appdetails/appdetails.component.css'],
  providers: [AppsService, AppImagesService, AppInstanceService, SecurePipe, AppRestartModalComponent, LocalDatePipe]
})
export class AppInstanceComponent implements OnInit, OnDestroy, AfterViewChecked {

  public defaultTooltipOptions = {
    'placement': 'bottom',
    'show-delay': '50',
    'theme': 'dark'
  };

  public AppInstanceState = AppInstanceState;

  @ViewChild(AppInstanceProgressComponent)
  public appInstanceProgress: AppInstanceProgressComponent;

  @ViewChild(AppRestartModalComponent)
  public modal: AppRestartModalComponent;

  @ViewChild(ModalComponent)
  public undeployModal: ModalComponent;

  @ViewChild('updateConfig')
  public updateConfigModal: ModalComponent;

  @ViewChild(RateComponent)
  public readonly appRate: RateComponent;

  app: Application;


  public p_first = 'p_first';

  public maxItemsOnPage = 6;
  public pageNumber = 1;

  public appInstanceStatus: AppInstanceStatus;

  public appInstanceId: number;
  public appInstance: AppInstance;
  public appInstanceStateHistory: AppInstanceStateHistory[];
  public configurationTemplate: any;
  public configurationUpdateTemplate: any;
  public submission: any = { data: {} };
  public isSubmissionUpdated = false;
  public isUpdateFormValid = false;
  public appConfiguration: AppConfiguration;

  public intervalCheckerSubscription;

  public wasUpdated = false;
  public refreshForm: EventEmitter<any>;
  public refreshUpdateForm: EventEmitter<any>;
  public readonly REPLACE_TEXT = '"insert-app-instances-here"';

  constructor(private appsService: AppsService,
    public appImagesService: AppImagesService,
    public userData: UserDataService,
    private appInstanceService: AppInstanceService,
    private router: Router,
    private route: ActivatedRoute,
    public translateState: TranslateStateModule,
    private location: Location,
    private translateService: TranslateService,
    private sessionService: SessionService,
    private domainService: DomainService,
    @Inject(LOCAL_STORAGE) public storage: StorageService) {}

  ngOnInit() {
    this.dateFormatChanges();
    this.appConfiguration = new AppConfiguration();
    this.route.params.subscribe(params => {
      this.appInstanceId = +params['id'];

      this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(appInstance => {
        this.appInstance = appInstance;
        this.configurationTemplate = this.getTemplate(appInstance.configWizardTemplate.template);
        this.refreshForm = new EventEmitter();
        this.refreshUpdateForm = new EventEmitter();
        this.submission.data.configuration = JSON.parse(appInstance.configuration);
        this.appsService.getApp(this.appInstance.applicationId).subscribe(app => {
          this.app = app;
          if (!isNullOrUndefined(this.app.configUpdateWizardTemplate)) {
              this.configurationUpdateTemplate = this.getTemplate(this.app.configUpdateWizardTemplate.template);
              const validation = {
                  min: 1,
                  max: 100,
              };
              this.domainService.getOne(this.appInstance.domainId).subscribe(
                  domain => {
                      validation.max = domain.applicationStatePerDomain.find(x => x.applicationBaseName == this.app.name).pvStorageSizeLimit;
                      console.log(validation);
                      this.refreshForm.emit({
                          property: 'form',
                          value: this.addValidationToConfigurationTemplateSpecificElement({key: "storageSpace"}, validation),
                      });
                      console.log(this.configurationTemplate)
                  }
              );
          }
        });
      });

      this.updateAppInstanceState();
      this.intervalCheckerSubscription = interval(5000).subscribe(() => this.updateAppInstanceState());
      this.undeployModal.setModalType('warning');
      this.undeployModal.setStatusOfIcons(true);
    });
  }

  dateFormatChanges(): void {
    this.sessionService.registerCulture(this.translateService.currentLang);
  }

  ngAfterViewChecked(): void {
  }

  public getStateAsString(state: any): string {
    return typeof state === 'string' && isNaN(Number(state.toString())) ? state : ApplicationState[state];
  }

  private recursiveSearchObjectToAddElementWhenKeyMatches(target: any, key: any, element: any) {
      if(!target){
          return;
      }
      if(Array.isArray(target)) {
          for (let t of target) {
              this.recursiveSearchObjectToAddElementWhenKeyMatches(t, key, element);
          }
      } else if (typeof target === 'object') {
          if(target.key === key.key) {
              console.log(target);
              target.validate = element;
          } else {
              for(const k of Object.keys(target)) {
                  this.recursiveSearchObjectToAddElementWhenKeyMatches(target[k], key, element);
              }
          }
      }
  }

  private addValidationToConfigurationTemplateSpecificElement(contains: any, validation: any): any {
      this.recursiveSearchObjectToAddElementWhenKeyMatches(this.configurationTemplate, contains, validation);
      return this.configurationTemplate;
  }

  changeForm() {
    if (!this.wasUpdated) {
      let temp = JSON.stringify(this.configurationTemplate);
      if (temp.match(this.REPLACE_TEXT)) {
        this.appInstanceService.getRunningAppInstances(this.appInstance.domainId).subscribe(apps => {
          temp = temp.replace('"insert-app-instances-here"', JSON.stringify(this.getRunningAppsMap(apps)));
          this.refreshForm.emit({
            property: 'form',
            value: JSON.parse(temp)
          });
        });
      }
      this.wasUpdated = true;
    }
  }

  private getRunningAppsMap(apps: AppInstance[]): any {
    const appMap = [];
    apps = this.filterRunningApps(apps);
    apps.forEach(app => appMap.push({value: app.descriptiveDeploymentId, label: app.name}));
    return appMap;
  }

  private filterRunningApps(apps: AppInstance[]): AppInstance[] {
    switch (this.app.name) {
      case 'Grafana':
        return apps.filter(app => app.applicationName === 'Prometheus');
      default:
        return apps;
    }
  }

  private updateAppInstanceState() {
    this.appInstanceService.getAppInstanceState(this.appInstanceId).subscribe(
      appInstanceStatus => {
        console.log('Type: ' + typeof appInstanceStatus.state + ', ' + appInstanceStatus.state);
        this.appInstanceStatus = appInstanceStatus;
        if (this.appInstanceStatus.state === this.AppInstanceState.FAILURE) {
          document.getElementById('app-prop').scrollLeft =
            (document.getElementsByClassName('stepwizard-btn-success').length * 180 +
              document.getElementsByClassName('stepwizard-btn-danger').length * 180);
        }
        this.appInstanceProgress.activeState = this.appInstanceStatus.state;
        this.appInstanceProgress.previousState = this.appInstanceStatus.previousState;
        document.getElementById('app-prop').scrollLeft =
          (document.getElementsByClassName('stepwizard-btn-success').length * 180 +
            document.getElementsByClassName('stepwizard-btn-danger').length * 180);
        if (AppInstanceState[AppInstanceState[this.appInstanceStatus.state]] === AppInstanceState[AppInstanceState.RUNNING]) {
          if (this.storage.has('appConfig_' + this.appInstanceId.toString())) {
            this.storage.remove('appConfig_' + this.appInstanceId.toString());
          }
          if (!this.appInstance || !this.appInstance.serviceAccessMethods) {
            this.updateAppInstance();
          }
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
      console.log('updated app instance url: ' + appInstance.serviceAccessMethods.find((sam : ServiceAccessMethod) => sam.type === ServiceAccessMethodType.DEFAULT).url);
      this.appInstance = appInstance;
    });
  }

  ngOnDestroy() {
    if (this.intervalCheckerSubscription) {
      this.intervalCheckerSubscription.unsubscribe();
    }
  }

  public redeploy(): void {
    this.appInstanceService.redeployAppInstance(this.appInstanceId).subscribe(() => console.debug('Redeployed'));
  }

  public removalFailed(): void {
    console.debug('Removing failed test...');
    this.appInstanceService.removeFailedInstance(this.appInstanceId).subscribe(() => console.debug('Removed failed instance'));
  }

  public changeAdditionalParameters(additionalParameters: any): void {
    if (!isNullOrUndefined(additionalParameters)) {
      this.appConfiguration.additionalParameters = additionalParameters;
    }
  }

  public changeMandatoryParameters(mandatoryParameters: any): void {
    if (!isNullOrUndefined(mandatoryParameters)) {
      this.appConfiguration.mandatoryParameters = mandatoryParameters;
    }
  }

  public changeAccessCredentials(accessCredentials: any): void {
      if (!isNullOrUndefined(accessCredentials)) {
          this.appConfiguration.accessCredentials = accessCredentials;
      }
  }

  public changeConfiguration(configuration: any): void {
    if (!isNullOrUndefined(configuration)) {
      this.appConfiguration.jsonInput = configuration;
    }
  }

  public applyConfiguration(input: any): void {
    if (!isNullOrUndefined(input['advanced'])) {
      this.appConfiguration.storageSpace = input['advanced'].storageSpace;
    }
    this.changeMandatoryParameters(input['mandatoryParameters']);
    this.changeAdditionalParameters(input['additionalParameters']);
    this.changeConfiguration(input['configuration']);
    this.changeAccessCredentials(input['accessCredentials']);
    if (isNullOrUndefined(this.appConfiguration.jsonInput)) {
        this.appConfiguration.jsonInput = {};
    }
    this.appInstanceService.applyConfiguration(this.appInstanceId, this.appConfiguration).subscribe(
        () => {
        console.log('Configuration applied');
        this.storage.set('appConfig_' + this.appInstanceId.toString(), this.appConfiguration);
        },
        (error) => {
        console.error(error);
        //TODO submission error message
        throw "Invalid submission";
        });
  }

  public updateConfiguration(): void {
      this.appInstanceService.updateConfiguration(this.appInstanceId, this.appConfiguration).subscribe(() => {
        console.log('Configuration updated');
        this.updateConfigModal.hide();
      });
  }

  public changeConfigUpdate(input: any): void {
    if (!isNullOrUndefined(input) && !isNullOrUndefined(input['data'])) {
      this.isUpdateFormValid = input['isValid'];
      this.changeConfiguration(input['data']['configuration']);
      this.changeAccessCredentials(input['data']['accessCredentials']);
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

  public getPathUrl(id: number): string {
      if (!isNullOrUndefined(id) && !isNaN(id)) {
          return '/apps/' + id + '/rate/my';
      } else {
          return '';
      }
  }

  public getConfigurationModal() {
    this.appInstanceService.getConfiguration(this.appInstanceId).subscribe(config => {
      this.appInstance.configuration = config;
      this.submission['data']['configuration'] = config;
      this.refreshUpdateForm.emit({
        property: 'submission',
        value: this.submission
      });
      this.isSubmissionUpdated = true;
      this.updateConfigModal.show();
    });
  }

  public closeConfigurationModal() {
    this.isSubmissionUpdated = false;
    this.updateConfigModal.hide();
  }

}
