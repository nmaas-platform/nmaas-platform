import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {ModalComponent} from '../../../shared/modal';
import {ConfigWizardTemplate} from '../../../model';
import {ConfigFileTemplate} from '../../../model/configfiletemplate';
import {AppImagesService, AppsService} from '../../../service';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigTemplateService} from '../../../service/configtemplate.service';
import {ParameterType} from '../../../model/parametertype';
import {KubernetesTemplate} from '../../../model/kubernetestemplate';
import {TranslateService} from '@ngx-translate/core';
import {DomSanitizer} from '@angular/platform-browser';
import {ApplicationState} from '../../../model/application-state';
import {KubernetesChart} from '../../../model/kuberneteschart';
import {AppStorageVolume} from '../../../model/app-storage-volume';
import {parseServiceStorageVolumeType, ServiceStorageVolumeType} from '../../../model/service-storage-volume';
import {AppAccessMethod} from '../../../model/app-access-method';
import {parseServiceAccessMethodType, ServiceAccessMethodType} from '../../../model/service-access-method';
import {AbstractControl, ValidatorFn} from '@angular/forms';
import {MultiSelect} from 'primeng/multiselect';
import {MenuItem, SelectItem} from 'primeng/api';
import {ApplicationDTO} from '../../../model/application-dto';

export function noParameterTypeInControlValueValidator(): ValidatorFn {

    const labels = Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];

    return (control: AbstractControl): { [key: string]: any } | null => {
        if (!(typeof control.value === 'string')) {
            return null;
        }
        const notValid = labels.filter(val => control.value.includes(val)).length === 0;
        console.log('checking: ', control.value, 'valid: ', !notValid);
        return notValid ? {'noParameterTypeInControlValue': {value: control.value}} : null;
    };
}

@Component({
    selector: 'app-appversioncreatewizard',
    templateUrl: './appversioncreatewizard.component.html',
    styleUrls: ['./appversioncreatewizard.component.css']
})
export class AppVersionCreateWizardComponent extends BaseComponent implements OnInit {


    @ViewChild(ModalComponent, {static: true})
    public modal: ModalComponent;

    @ViewChild('tagsMultiSelect')
    public tagsMultiSelect: MultiSelect;

    public applicationDTO: ApplicationDTO;
    public appName: string;
    public steps: MenuItem[];
    public activeStepIndex = 0;
    public rulesAccepted = false;
    public deployParameter: SelectItem[] = [];
    public selectedDeployParameters: string[] = [];
    public errorMessage: string = undefined;
    public configFileTemplates: ConfigFileTemplate[] = [];
    public addConfigUpdate = false;
    public basicAuth = false;
    public formDisplayChange = true;
    public logo: any[] = [];
    public screenshots: any[] = [];

    // properties for global parameters deploy validation
    // in future extensions pack this into single object
    public deployParamKeyValidator: ValidatorFn = noParameterTypeInControlValueValidator();
    public keyValidatorMessage: string = 'Key name should contain one of following values: ' + this.getParametersTypes().join(', ');
    public keyErrorKey = 'noParameterTypeInControlValue';

    public defaultTooltipOptions = {
        'placement': 'right',
        'show-delay': '50',
        'theme': 'dark'
    };

    constructor(public appsService: AppsService, public route: ActivatedRoute, public translate: TranslateService, public dom: DomSanitizer,
                public configTemplateService: ConfigTemplateService, public router: Router, public appImagesService: AppImagesService) {
        super();
    }

    ngOnInit() {
        this.modal.setModalType('success');
        this.modal.setStatusOfIcons(false);
        this.mode = this.getMode(this.route);
        this.getParametersTypes().forEach(val => this.deployParameter.push({label: val.replace('_', ' '), value: val}));
        this.steps = [
            {label: this.translate.instant('APPS_WIZARD.GENERAL_INFO_STEP')},
            {label: this.translate.instant('APPS_WIZARD.BASIC_APP_INFO_STEP')},
            {label: this.translate.instant('APPS_WIZARD.APP_DEPLOYMENT_SPEC_STEP')},
            {label: this.translate.instant('APPS_WIZARD.CONFIG_TEMPLATES_STEP')},
            {label: this.translate.instant('APPS_WIZARD.SHORT_REVIEW_STEP')}
        ];
        this.route.params.subscribe(params => {
            const appName = params['name']
            const appId = params['id']
            if (appName != null) {
                this.appsService.getLatestVersion(params['name']).subscribe(
                    (result: ApplicationDTO) => {
                        this.applicationDTO = result;
                        this.applicationDTO.application.version = undefined;
                        this.applicationDTO.application.state = ApplicationState.NEW;
                        this.appName = appName;
                        this.fillWizardWithData(result);
                    },
                    err => this.handleError(err)
                );
            } else if (appId != null) {
                this.appsService.getApplicationDTO(appId).subscribe(
                    (result: ApplicationDTO) => {
                        this.applicationDTO = result;
                        this.appName = result.application.name;
                        this.fillWizardWithData(result);
                    },
                    err => this.handleError(err)
                );
                this.rulesAccepted = true;
                this.activeStepIndex = 1;
            }
        });
    }

    public handleError(err: any): void {
        console.error(err);
        if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
            this.router.navigateByUrl('/notfound');
        }
    }

    public fillWizardWithData(appToEdit: ApplicationDTO): void {

        console.log(appToEdit);

        const temp: Map<ParameterType, string> = new Map();
        Object.keys(appToEdit.application.appDeploymentSpec.deployParameters).forEach(key => {
            temp.set(ParameterType[key], appToEdit.application.appDeploymentSpec.deployParameters[key]);
            this.selectedDeployParameters.push(key);
        });
        // do not override current type (object) to map, because JS map cannot be serialized to JSON
        // this.app.appDeploymentSpec.deployParameters = temp;

        if (this.applicationDTO.application.configWizardTemplate == null) {
            this.applicationDTO.application.configWizardTemplate = new ConfigWizardTemplate();
            this.applicationDTO.application.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
        }
        this.getLogo(appToEdit.applicationBase.id);
        this.getScreenshots(appToEdit.applicationBase.id);

        if (appToEdit.application.appDeploymentSpec.kubernetesTemplate == null) {
            this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate = new KubernetesTemplate();
        }
        if (appToEdit.application.appDeploymentSpec.kubernetesTemplate.chart == null) {
            this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate.chart = new KubernetesChart();
        }
        if (this.applicationDTO.application.appConfigurationSpec.templates.length > 0) {
            this.configFileTemplates = this.applicationDTO.application.appConfigurationSpec.templates;
        } else {
            this.configFileTemplates.push(new ConfigFileTemplate());
        }
        this.basicAuth = this.hasAlreadyBasicAuth();
        this.addConfigUpdate = (this.applicationDTO.application.configUpdateWizardTemplate != null);
    }

    public nextStep(): void {
        this.activeStepIndex += 1;
    }

    public previousStep(): void {
        this.errorMessage = undefined;
        this.activeStepIndex -= 1;
    }

    public addApplication(): void {
        if (this.templateHasContent()) {
            this.applicationDTO.application.appConfigurationSpec.templates = this.configFileTemplates;
        }
        if (!this.isChartValid()) {
            this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate.chart = undefined;
        }
        const app = this.applicationDTO.application;
        app.id = null
        this.appsService.createApplication(app).subscribe(
            () => {
                this.errorMessage = undefined;
                this.modal.show();
            },
            error => this.errorMessage = error.message
        );
    }

    public updateApplication(): void {
        if (this.templateHasContent()) {
            this.applicationDTO.application.appConfigurationSpec.templates = this.configFileTemplates;
        }
        if (!this.isChartValid()) {
            this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate.chart = undefined;
        }
        const app = this.applicationDTO.application;
        this.appsService.updateApplication(app).subscribe(
            () => {
                this.errorMessage = undefined;
                this.modal.show();
            },
            error => this.errorMessage = error.message
        );
    }

    public templateHasContent(): boolean {
        return this.configFileTemplates.length > 0 &&
            (this.configFileTemplates[0].configFileName != null) &&
            (this.configFileTemplates[0].configFileTemplateContent != null);
    }

    public isChartValid(): boolean {
        return (this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate.chart != null)
            && this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate.chart.name !== ''
            && this.applicationDTO.application.appDeploymentSpec.kubernetesTemplate.chart.version !== '';
    }

    public changeRulesAcceptedFlag(): void {
        this.rulesAccepted = !this.rulesAccepted;
    }

    public setConfigTemplate(event): void {
        if (!this.applicationDTO.application.configWizardTemplate) {
            this.applicationDTO.application.configWizardTemplate = new ConfigWizardTemplate();
        }
        this.applicationDTO.application.configWizardTemplate.template = event.form;
    }

    public setUpdateConfigTemplate(event): void {
        if (!this.applicationDTO.application.configUpdateWizardTemplate) {
            this.applicationDTO.application.configUpdateWizardTemplate = new ConfigWizardTemplate();
        }
        this.applicationDTO.application.configUpdateWizardTemplate.template = event.form;
    }

    // deploy params selection types methods

    public getParametersTypes(): string[] {
        return Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];
    }

    public addToDeployParametersMap(key: string, event) {
        this.applicationDTO.application.appDeploymentSpec.deployParameters[key] = event.target.value;
    }

    public getDeployParameterValue(key: string): string {
        return this.applicationDTO.application.appDeploymentSpec.deployParameters[key] || '';
    }

    public removeDeployParameterFromMap(event) {
        if (!event.value.some(val => val === event.itemValue)) {
            delete this.applicationDTO.application.appDeploymentSpec.deployParameters[event.itemValue as string];
        }
    }

    // handle config methods

    public addConfig() {
        const newTemplate = new ConfigFileTemplate();
        newTemplate.applicationId = this.applicationDTO.application.id;
        this.configFileTemplates.push(newTemplate);
    }

    public removeConfig(id: number) {
        this.configFileTemplates.splice(id, 1);
    }

    public hasAlreadyBasicAuth(): boolean {
        if (this.applicationDTO.application.configWizardTemplate == null) {
            return false;
        }
        const config: string = JSON.stringify(this.applicationDTO.application.configWizardTemplate.template);
        return config.search(/accessCredentials/g) !== -1 &&
            config.search(/accessUsername/g) !== -1 &&
            config.search(/accessPassword/g) !== -1;
    }

    public handleBasicAuth() {
        if (!this.applicationDTO.application.appConfigurationSpec.configFileRepositoryRequired
            && (this.applicationDTO.application.configWizardTemplate == null)
        ) {
            this.applicationDTO.application.configWizardTemplate = new ConfigWizardTemplate();
            this.applicationDTO.application.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
        }
        if (this.basicAuth) {
            this.addBasicAuth();
        } else {
            this.removeBasicAuth();
        }
    }

    public addBasicAuth(): any {
        const config = this.getNestedObject(
            this.applicationDTO.application.configWizardTemplate.template,
            ['components', 0, 'components', 0, 'components']
        );
        if (config != null) {
            config.unshift(this.configTemplateService.getBasicAuth(this.applicationDTO.application.name));
        }
        if (this.applicationDTO.application.configUpdateWizardTemplate == null) {
            this.applicationDTO.application.configUpdateWizardTemplate = new ConfigWizardTemplate();
            this.applicationDTO.application.configUpdateWizardTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
        }
        this.applicationDTO.application.configUpdateWizardTemplate.template.components
            .unshift(this.configTemplateService.getBasicAuth(this.applicationDTO.application.name));
    }

    public removeBasicAuth(): any {
        const config = this.getNestedObject(
            this.applicationDTO.application.configWizardTemplate.template,
            ['components', 0, 'components', 0, 'components']
        );
        if (config != null) {
            const index = config.findIndex(val => val.key === 'accessCredentials');
            config.splice(index, 1);
        }
        this.applicationDTO.application.configUpdateWizardTemplate.template.components =
            this.applicationDTO.application.configUpdateWizardTemplate.template.components.filter(val => val.key !== 'accessCredentials');
        this.removeEmptyUpdateConfig();
    }

    public removeEmptyUpdateConfig(): void {
        const updateConfig = this.getNestedObject(
            this.applicationDTO.application.configUpdateWizardTemplate.template,
            ['components', 0, 'components']
        );
        if ((updateConfig == null) || updateConfig.length === 0) {
            this.applicationDTO.application.configUpdateWizardTemplate = undefined;
            this.addConfigUpdate = false;
        }
    }

    public handleConfigTemplate(): any {
        if (this.addConfigUpdate && (this.applicationDTO.application.configUpdateWizardTemplate == null)) {
            this.applicationDTO.application.configUpdateWizardTemplate = new ConfigWizardTemplate();
            this.applicationDTO.application.configUpdateWizardTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
        }
        if (!this.addConfigUpdate && !this.hasAlreadyBasicAuth()) {
            this.applicationDTO.application.configUpdateWizardTemplate = undefined;
        }
    }

    public changeBasicAuthInForms() {
        this.formDisplayChange = false;
        this.handleBasicAuth();
        setTimeout(() => {
            this.formDisplayChange = true
        }, 1);
    }

    public changeDefaultElementInForms() {
        this.formDisplayChange = false;
        this.handleDefaultElement();
        setTimeout(() => {
            this.formDisplayChange = true
        }, 1);
    }

    public handleDefaultElement() {
        if (this.applicationDTO.application.appConfigurationSpec.configFileRepositoryRequired) {
            this.removeDefaultElement();
        } else {
            this.addDefaultElement();
            this.removeElementsFromUpdateConfig();
        }
    }

    public addDefaultElement(): void {
        let config = this.getNestedObject(
            this.applicationDTO.application.configWizardTemplate.template,
            ['components', 0, 'components', 0, 'components']
        );
        if ((config != null) && (config.find(val => val.key === 'configuration') != null)) {
            config = config.find(val => val.key === 'configuration');
            config.components.length = 0;
            config.components.push(this.configTemplateService.getDefaultElement());
        }
    }

    public removeDefaultElement(): void {
        const config = this.getNestedObject(
            this.applicationDTO.application.configWizardTemplate.template,
            ['components', 0, 'components', 0, 'components']
        );
        if ((config != null) && (config.find(val => val.key === 'configuration') != null)) {
            config.find(val => val.key === 'configuration').components.length = 0;
        }
    }

    public removeElementsFromUpdateConfig(): void {
        if (this.applicationDTO.application.configUpdateWizardTemplate != null) {
            const config = this.getNestedObject(this.applicationDTO.application.configUpdateWizardTemplate.template, ['components']);
            if ((config != null) && (config.find(val => val.key === 'configuration') != null)) {
                config.find(val => val.key === 'configuration').components.length = 0;
            }
            this.removeEmptyUpdateConfig();
        }
    }

    getNestedObject = (nestedObj, pathArr) => {
        return pathArr.reduce((obj, key) =>
            (obj && obj[key] !== 'undefined') ? obj[key] : undefined, nestedObj);
    };

    public getLogo(id: number): void {
        this.appImagesService.getLogoFile(id).subscribe(file => {
            this.logo.push(this.convertToProperImageFile(file));
        }, err => console.error(err.message));
    }


    public getScreenshots(id: number): void {
        this.appImagesService.getAppScreenshotsUrls(id).subscribe(fileInfo => {
            fileInfo.forEach(val => {
                this.appImagesService.getAppScreenshotFile(id, val.id).subscribe(img => {
                    this.screenshots.push(this.convertToProperImageFile(img));
                }, err => console.error(err.message));
            });
        }, err => console.error(err.message));
    }

    private convertToProperImageFile(file: any) {
        const result: any = new File([file], 'uploaded file', {type: file.type});
        result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
        return result;
    }

    public addNewAccessMethod(): void {
        this.applicationDTO.application.appDeploymentSpec.accessMethods.push(new AppAccessMethod())
    }

    public accessMethodTypeOptions(): string[] {
        const keys: Set<string> = new Set(Object.keys(ServiceAccessMethodType));
        if (this.applicationDTO.application.appDeploymentSpec.accessMethods
            .find(p => parseServiceAccessMethodType(p.type) === ServiceAccessMethodType.DEFAULT)) {
            keys.delete(ServiceAccessMethodType[ServiceAccessMethodType.DEFAULT]);
        }
        return Array.from(keys);
    }

    public removeAccessMethod(event): void {
        this.applicationDTO.application.appDeploymentSpec.accessMethods.splice(event, 1);
    }

    public addNewStorageVolume(): void {
        this.applicationDTO.application.appDeploymentSpec.storageVolumes.push(new AppStorageVolume())
    }

    public storageVolumeTypeOptions(): string[] {
        const keys: Set<string> = new Set(Object.keys(ServiceStorageVolumeType));
        if (this.applicationDTO.application.appDeploymentSpec.storageVolumes
            .find(p => parseServiceStorageVolumeType(p.type) === ServiceStorageVolumeType.MAIN)
        ) {
            keys.delete(ServiceStorageVolumeType[ServiceStorageVolumeType.MAIN]);
        }
        return Array.from(keys);
    }

    public removeStorageVolume(event): void {
        this.applicationDTO.application.appDeploymentSpec.storageVolumes.splice(event, 1);
    }

}
