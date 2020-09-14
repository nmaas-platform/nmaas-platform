import {Component, EventEmitter, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {AppImagesService, AppInstanceService, AppsService} from '../../../service';
import {AppInstanceProgressComponent} from '../appinstanceprogress';
import {AppInstance, AppInstanceProgressStage, AppInstanceState, AppInstanceStatus, Application} from '../../../model';
import {AppInstanceExtended} from '../../../model/appinstanceextended';
import {SecurePipe} from '../../../pipe';
import {AppRestartModalComponent} from '../modals/apprestart';
import {AppAbortModalComponent} from '../modals/app-abort-modal';
import {AppInstanceStateHistory} from '../../../model/appinstancestatehistory';
import {RateComponent} from '../../../shared/rate';
import {AppConfiguration} from '../../../model/app-configuration';
import {isNullOrUndefined} from 'util';
import {LOCAL_STORAGE, StorageService} from 'ngx-webstorage-service';
import {ModalComponent} from '../../../shared/modal';
import {interval} from 'rxjs/internal/observable/interval';
import {TranslateService} from '@ngx-translate/core';
import {SessionService} from '../../../service/session.service';
import {LocalDatePipe} from '../../../pipe/local-date.pipe';
import {ApplicationState} from '../../../model/applicationstate';
import {ServiceAccessMethodType} from '../../../model/serviceaccessmethod';
import {AccessMethodsModalComponent} from '../modals/access-methods-modal/access-methods-modal.component';
import {ShellClientService} from '../../../service/shell-client.service';
import {PodInfo} from '../../../model/podinfo';

@Component({
    selector: 'nmaas-appinstance',
    templateUrl: './appinstance.component.html',
    styleUrls: ['./appinstance.component.css', '../../appdetails/appdetails.component.css'],
    providers: [AppsService, AppImagesService, AppInstanceService, SecurePipe, AppRestartModalComponent, AppAbortModalComponent, LocalDatePipe]
})
export class AppInstanceComponent implements OnInit, OnDestroy {

    public defaultTooltipOptions = {
        'placement': 'bottom',
        'show-delay': '50',
        'theme': 'dark'
    };

    public AppInstanceState = AppInstanceState;

    @ViewChild(AppInstanceProgressComponent)
    public appInstanceProgress: AppInstanceProgressComponent;

    @ViewChild(AppRestartModalComponent)
    public appRestartModal: AppRestartModalComponent;

    @ViewChild(ModalComponent)
    public undeployModal: ModalComponent;

    @ViewChild(AppAbortModalComponent)
    public appAbortModal: AppAbortModalComponent;

    @ViewChild('updateConfig')
    public updateConfigModal: ModalComponent;

    @ViewChild(AccessMethodsModalComponent)
    public accessMethodsModal: AccessMethodsModalComponent;

    @ViewChild(RateComponent)
    public readonly appRate: RateComponent;

    app: Application;


    public p_first = 'p_first';

    public maxItemsOnPage = 6;
    public pageNumber = 1;

    public appInstanceStatus: AppInstanceStatus;

    public appInstanceId: number;
    public appInstance: AppInstanceExtended;
    public appInstanceStateHistory: AppInstanceStateHistory[];
    public configurationTemplate: any;
    public configurationUpdateTemplate: any;
    public submission: any = {data: {}};
    public isSubmissionUpdated = false;
    public isUpdateFormValid = false;
    public appConfiguration: AppConfiguration;

    public intervalCheckerSubscription;

    public wasUpdated = false;
    public refreshForm: EventEmitter<any>;
    public refreshUpdateForm: EventEmitter<any>;
    public readonly REPLACE_TEXT = '"insert-app-instances-here"';

    public podNames: PodInfo[] = [];

    constructor(private appsService: AppsService,
                public appImagesService: AppImagesService,
                private appInstanceService: AppInstanceService,
                public router: Router,
                private route: ActivatedRoute,
                private location: Location,
                private translateService: TranslateService,
                private sessionService: SessionService,
                private shellClientService: ShellClientService,
                @Inject(LOCAL_STORAGE) public storage: StorageService) {
    }

    ngOnInit() {
        this.dateFormatChanges();
        this.appConfiguration = new AppConfiguration();
        this.route.params.subscribe(params => {
            this.appInstanceId = +params['id'];

            this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(
                appInstance => {
                    this.refreshForm = new EventEmitter();
                    this.refreshUpdateForm = new EventEmitter();

                    this.appInstance = appInstance;
                    this.configurationTemplate = this.getTemplate(appInstance.configWizardTemplate.template);
                    this.app = appInstance.application;

                    this.updateAppInstancePodNames();

                    this.submission.data.configuration = JSON.parse(appInstance.configuration);

                    if (!isNullOrUndefined(this.appInstance.configUpdateWizardTemplate)) {
                        this.configurationUpdateTemplate = this.getTemplate(this.appInstance.configUpdateWizardTemplate.template);
                    }

                    // apply validation from application state per domain
                    const validation = {
                        min: 1,
                        max: 100,
                    };
                    validation.max = appInstance.domain.applicationStatePerDomain
                        .find(x => x.applicationBaseName === this.appInstance.applicationName).pvStorageSizeLimit;
                    this.refreshForm.emit({
                        property: 'form',
                        value: this.addValidationToConfigurationTemplateSpecificElement({key: 'storageSpace'}, validation),
                    });
                },
                err => {
                    console.error(err);
                    if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
                        this.router.navigateByUrl('/notfound');
                    }
                });

            this.updateAppInstanceState();
            this.intervalCheckerSubscription = interval(5000).subscribe(() => this.updateAppInstanceState());

            // TODO fix after modal init
            console.log('Setting undeploy modal params')
            this.undeployModal.setModalType('warning');
            this.undeployModal.setStatusOfIcons(true);
        });
    }

    dateFormatChanges(): void {
        this.sessionService.registerCulture(this.translateService.currentLang);
    }

    public getStateAsString(state: any): string {
        return typeof state === 'string' && isNaN(Number(state.toString())) ? state : ApplicationState[state];
    }

    public getStateAsEnum(state: string | AppInstanceState): AppInstanceState {
        return typeof state === 'string' ? AppInstanceState[state] : state;
    }

    // Apply Domain constraints to configuration form
    private recursiveSearchObjectToAddElementWhenKeyMatches(target: any, key: any, element: any) {
        if (!target) {
            return;
        }
        if (Array.isArray(target)) {
            for (const t of target) {
                this.recursiveSearchObjectToAddElementWhenKeyMatches(t, key, element);
            }
        } else if (typeof target === 'object') {
            if (target.key === key.key) {
                console.log(target);
                target.validate = element;
            } else {
                for (const k of Object.keys(target)) {
                    this.recursiveSearchObjectToAddElementWhenKeyMatches(target[k], key, element);
                }
            }
        }
    }

    private addValidationToConfigurationTemplateSpecificElement(contains: any, validation: any): any {
        this.recursiveSearchObjectToAddElementWhenKeyMatches(this.configurationTemplate, contains, validation);
        return this.configurationTemplate;
    }

    /**
     * updates form with retrieved application values
     * aim is to provide certain applications access to another instances
     * e.g. provide access to prometheus instance for Grafana
     */
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

    /**
     * converts list of AppInstances into key-value (label) pairs to be used in select input type
     * @param apps
     */
    private getRunningAppsMap(apps: AppInstance[]): any {
        const appMap = [];
        apps = this.filterRunningApps(apps);
        apps.forEach(app => appMap.push({value: app.descriptiveDeploymentId, label: app.name}));
        return appMap;
    }

    /**
     * filter list of applications to provide access to related applications
     * e.g. for Grafana return only running Prometheus instances
     * @param apps
     */
    private filterRunningApps(apps: AppInstance[]): AppInstance[] {
        switch (this.appInstance.applicationName) {
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

                // TODO refactor scroll
                const appPropElement: HTMLElement = document.getElementById('app-prop');
                const stepWizardBtnSuccessListLength: number = document.getElementsByClassName('stepwizard-btn-success').length;
                const stepWizardBtnDangerListLength: number = document.getElementsByClassName('stepwizard-btn-danger').length;

                if (this.appInstanceStatus.state === this.AppInstanceState.FAILURE) {
                    if (appPropElement) {
                        document.getElementById('app-prop').scrollLeft =
                            (document.getElementsByClassName('stepwizard-btn-success').length * 180 +
                                document.getElementsByClassName('stepwizard-btn-danger').length * 180);
                    }
                }

                this.appInstanceProgress.activeState = this.appInstanceStatus.state;
                this.appInstanceProgress.previousState = this.appInstanceStatus.previousState;

                if (appPropElement) {
                    document.getElementById('app-prop').scrollLeft =
                        (document.getElementsByClassName('stepwizard-btn-success').length * 180 +
                            document.getElementsByClassName('stepwizard-btn-danger').length * 180);
                }

                if (AppInstanceState[AppInstanceState[this.appInstanceStatus.state]] === AppInstanceState[AppInstanceState.RUNNING]) {
                    console.log('app instance is running');
                    if (this.storage.has('appConfig_' + this.appInstanceId.toString())) {
                        this.storage.remove('appConfig_' + this.appInstanceId.toString());
                    }
                    if (!this.appInstance || !this.appInstance.serviceAccessMethods) {
                        this.updateAppInstance();
                    }
                    console.log('is ssh access allowed: ' + this.appInstance.application.appDeploymentSpec.allowSshAccess);
                    console.log('array of pods has length: ' + this.podNames.length);
                    if (this.appInstance.application.appDeploymentSpec.allowSshAccess && !this.podNames.length) {
                        this.updateAppInstancePodNames();
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
            this.appInstance = appInstance;
        });
    }

    private updateAppInstancePodNames() {
        console.log('update list of available pods');
        this.shellClientService.getPossiblePods(this.appInstanceId).subscribe(pods => {
            this.podNames = pods;
        });
    }

    ngOnDestroy() {
        if (this.intervalCheckerSubscription) {
            this.intervalCheckerSubscription.unsubscribe();
        }
    }

    public redeploy(): void {
        this.appInstanceService.redeployAppInstance(this.appInstanceId).subscribe(() => console.log('Redeployed'));
    }

    public removalFailed(): void {
        this.appInstanceService.removeFailedInstance(this.appInstanceId).subscribe(() => console.log('Removed failed instance'));
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
                // TODO submission error message
                throw new Error('Invalid submission');
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
        this.updateConfigModal.show();
    }

    public closeConfigurationModal() {
        this.updateConfigModal.hide();
    }

    /**
     * converts URL to use https
     * @param url
     */
    public validateURL(url: string): string {
        if (isNullOrUndefined(url)) {
            return '';
        }
        if (url.startsWith('http://')) {
            return url.replace('http://', 'https://');
        }
        if (url.startsWith('https://')) {
            return url;
        }
        return 'https://' + url;
    }

    /**
     * converts string to corresponding ServiceAccessMethodType enum value
     * @param a
     */
    public accessMethodTypeAsEnum(a: ServiceAccessMethodType | string): ServiceAccessMethodType {
        if (typeof  a === 'string') {
            return ServiceAccessMethodType[a];
        }
        return a;
    }

    /**
     * checks if there is only one access method with type DEFAULT
     * returns true if conditions are satisfied
     */
    public shouldDisplayButton(): boolean {
        if (!this.appInstance) {
            return false;
        }
        if (!this.appInstance.serviceAccessMethods) {
            return false;
        }
        if (this.appInstance.serviceAccessMethods.length !== 1) {
            return false;
        }
        if (this.accessMethodTypeAsEnum(this.appInstance.serviceAccessMethods[0].type) !== ServiceAccessMethodType.DEFAULT) {
            return false;
        }
        return true;
    }

    /**
     * checks if there are multiple access methods or if one access method is not default
     * returns true if conditions are satisfied
     */
    public shouldDisplayModal(): boolean {
        if (!this.appInstance) {
            return false;
        }
        if (!this.appInstance.serviceAccessMethods) {
            return false;
        }
        if (this.appInstance.serviceAccessMethods.length > 1) {
            return true;
        }
        if (this.appInstance.serviceAccessMethods.length === 1 &&
            this.accessMethodTypeAsEnum(this.appInstance.serviceAccessMethods[0].type) !== ServiceAccessMethodType.DEFAULT) {
            return true;
        }
        return false;
    }

    public checkStatus(): void {
        this.appInstanceService.checkStatus(this.appInstanceId).subscribe(
            data => {
                console.log('Deployment verified');
            },
            error => {
                console.error(error);
            }
        )
    }

}
