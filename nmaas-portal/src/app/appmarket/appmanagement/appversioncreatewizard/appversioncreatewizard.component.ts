import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {ModalComponent} from '../../../shared/modal';
import {Application, ConfigWizardTemplate} from '../../../model';
import {ConfigFileTemplate} from '../../../model/configfiletemplate';
import {AppImagesService, AppsService} from '../../../service';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigTemplateService} from '../../../service/configtemplate.service';
import {isNullOrUndefined} from 'util';
import {ParameterType} from '../../../model/parametertype';
import {KubernetesTemplate} from '../../../model/kubernetestemplate';
import {TranslateService} from '@ngx-translate/core';
import {DomSanitizer} from '@angular/platform-browser';
import {ApplicationState} from '../../../model/applicationstate';
import {KubernetesChart} from '../../../model/kuberneteschart';
import {AppStorageVolume} from '../../../model/app-storage-volume';
import {ServiceStorageVolume, ServiceStorageVolumeType} from '../../../model/servicestoragevolume';
import {AppAccessMethod} from '../../../model/app-access-method';
import {ServiceAccessMethod, ServiceAccessMethodType} from '../../../model/serviceaccessmethod';
import {AbstractControl, ValidatorFn} from '@angular/forms';
import {MultiSelect} from 'primeng/multiselect';
import {MenuItem, SelectItem} from 'primeng/api';

export function noParameterTypeInControlValueValidator(): ValidatorFn {

    const labels = Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];

    return (control: AbstractControl): {[key: string]: any} | null => {
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


    @ViewChild(ModalComponent, { static: true })
    public modal: ModalComponent;

    @ViewChild('tagsMultiSelect')
    public tagsMultiSelect: MultiSelect;

    public app: Application;
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
            if (!isNullOrUndefined(params['name'])) {
                this.appsService.getLatestVersion(params['name']).subscribe(result => {
                        result.version = undefined;
                        result.state = ApplicationState.NEW;
                        this.app = result;
                        this.appName = result.name;
                        this.fillWizardWithData(result);
                    },
                    err => {
                        console.error(err);
                        if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
                            this.router.navigateByUrl('/notfound');
                        }
                    });
            } else if (!isNullOrUndefined(params['id'])) {
                this.appsService.getApp(params['id']).subscribe(result => {
                        this.app = result;
                        this.appName = result.name;
                        this.fillWizardWithData(result);
                    },
                    err => {
                        console.error(err);
                        if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
                            this.router.navigateByUrl('/notfound');
                        }
                    });
                this.rulesAccepted = true;
                this.activeStepIndex = 1;
            }
        });
    }

    public fillWizardWithData(appToEdit: Application): void {

        console.log(appToEdit);

        const temp: Map<ParameterType, string> = new Map();
        Object.keys(appToEdit.appDeploymentSpec.deployParameters).forEach(key => {
            temp.set(ParameterType[key], appToEdit.appDeploymentSpec.deployParameters[key]);
            this.selectedDeployParameters.push(key);
        });
        // do not override current type (object) to map, because JS map cannot be serialized to JSON
        // this.app.appDeploymentSpec.deployParameters = temp;

        if (isNullOrUndefined(this.app.configWizardTemplate)) {
            this.app.configWizardTemplate = new ConfigWizardTemplate();
            this.app.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
        }
        this.getLogo(appToEdit.id);
        this.getScreenshots(appToEdit.id);
        if (isNullOrUndefined(appToEdit.appDeploymentSpec.kubernetesTemplate)) {
            this.app.appDeploymentSpec.kubernetesTemplate = new KubernetesTemplate();
        }
        if (isNullOrUndefined(appToEdit.appDeploymentSpec.kubernetesTemplate.chart)) {
            this.app.appDeploymentSpec.kubernetesTemplate.chart = new KubernetesChart();
        }
        if (this.app.appConfigurationSpec.templates.length > 0) {
            this.configFileTemplates = this.app.appConfigurationSpec.templates;
        } else {
            this.configFileTemplates.push(new ConfigFileTemplate());
        }
        this.basicAuth = this.hasAlreadyBasicAuth();
        this.addConfigUpdate = !isNullOrUndefined(this.app.configUpdateWizardTemplate);
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
            this.app.appConfigurationSpec.templates = this.configFileTemplates;
        }
        if (!this.isChartValid()) {
            this.app.appDeploymentSpec.kubernetesTemplate.chart = undefined;
        }
        this.appsService.addApp(this.app).subscribe(() => {
            this.errorMessage = undefined;
            this.modal.show();
        }, error => this.errorMessage = error.message);
    }

    public updateApplication(): void {
        if (this.templateHasContent()) {
            this.app.appConfigurationSpec.templates = this.configFileTemplates;
        }
        if (!this.isChartValid()) {
            this.app.appDeploymentSpec.kubernetesTemplate.chart = undefined;
        }
        this.appsService.updateApp(this.app).subscribe(() => {
            this.errorMessage = undefined;
            this.modal.show();
        }, error => this.errorMessage = error.message);
    }

    public templateHasContent(): boolean {
        return this.configFileTemplates.length > 0 && !isNullOrUndefined(this.configFileTemplates[0].configFileName) &&
            !isNullOrUndefined(this.configFileTemplates[0].configFileTemplateContent);
    }

    public isChartValid(): boolean {
        return !isNullOrUndefined(this.app.appDeploymentSpec.kubernetesTemplate.chart)
            && this.app.appDeploymentSpec.kubernetesTemplate.chart.name !== ''
            && this.app.appDeploymentSpec.kubernetesTemplate.chart.version !== '';
    }

    public changeRulesAcceptedFlag(): void {
        this.rulesAccepted = !this.rulesAccepted;
    }

    public setConfigTemplate(event): void {
        if (!this.app.configWizardTemplate) {
            this.app.configWizardTemplate = new ConfigWizardTemplate();
        }
        this.app.configWizardTemplate.template = event.form;
    }

    public setUpdateConfigTemplate(event): void {
        if (!this.app.configUpdateWizardTemplate) {
            this.app.configUpdateWizardTemplate = new ConfigWizardTemplate();
        }
        this.app.configUpdateWizardTemplate.template = event.form;
    }

    // deploy params selection types methods

    public getParametersTypes(): string[] {
        return Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];
    }

    public addToDeployParametersMap(key: string, event) {
        this.app.appDeploymentSpec.deployParameters[key] = event.target.value;
    }

    public getDeployParameterValue(key: string): string {
        return this.app.appDeploymentSpec.deployParameters[key] || '';
    }

    public removeDeployParameterFromMap(event) {
        if (!event.value.some(val => val === event.itemValue)) {
            delete this.app.appDeploymentSpec.deployParameters[event.itemValue as string];
        }
    }

    // handle config methods

    public addConfig() {
        const newTemplate = new ConfigFileTemplate();
        newTemplate.applicationId = this.app.id;
        this.configFileTemplates.push(newTemplate);
    }

    public removeConfig(id: number) {
        this.configFileTemplates.splice(id, 1);
    }

    public hasAlreadyBasicAuth(): boolean {
        if (isNullOrUndefined(this.app.configWizardTemplate)) {
            return false;
        }
        const config: string = JSON.stringify(this.app.configWizardTemplate.template);
        return config.search(/accessCredentials/g) != -1 &&
            config.search(/accessUsername/g) != -1 &&
            config.search(/accessPassword/g) != -1;
    }

    public handleBasicAuth() {
        if (!this.app.appConfigurationSpec.configFileRepositoryRequired && isNullOrUndefined(this.app.configWizardTemplate)) {
            this.app.configWizardTemplate = new ConfigWizardTemplate();
            this.app.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
        }
        if (this.basicAuth) {
            this.addBasicAuth();
        } else {
            this.removeBasicAuth();
        }
    }

    public addBasicAuth(): any {
        const config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, 'components', 0, 'components']);
        if (!isNullOrUndefined(config)) {
            config.unshift(this.configTemplateService.getBasicAuth(this.app.name));
        }
        if (isNullOrUndefined(this.app.configUpdateWizardTemplate)) {
            this.app.configUpdateWizardTemplate = new ConfigWizardTemplate();
            this.app.configUpdateWizardTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
        }
        this.app.configUpdateWizardTemplate.template.components.unshift(this.configTemplateService.getBasicAuth(this.app.name));
    }

    public removeBasicAuth(): any {
        const config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, 'components', 0, 'components']);
        if (!isNullOrUndefined(config)) {
            const index = config.findIndex(val => val.key === 'accessCredentials');
            config.splice(index, 1);
        }
        this.app.configUpdateWizardTemplate.template.components = this.app.configUpdateWizardTemplate.template.components.filter(val => val.key != 'accessCredentials');
        this.removeEmptyUpdateConfig();
    }

    public removeEmptyUpdateConfig(): void {
        const updateConfig = this.getNestedObject(this.app.configUpdateWizardTemplate.template, ['components', 0, 'components']);
        if (isNullOrUndefined(updateConfig) || updateConfig.length === 0) {
            this.app.configUpdateWizardTemplate = undefined;
            this.addConfigUpdate = false;
        }
    }

    public handleConfigTemplate(): any {
        if (this.addConfigUpdate && isNullOrUndefined(this.app.configUpdateWizardTemplate)) {
            this.app.configUpdateWizardTemplate = new ConfigWizardTemplate();
            this.app.configUpdateWizardTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
        }
        if (!this.addConfigUpdate && !this.hasAlreadyBasicAuth()) {
            this.app.configUpdateWizardTemplate = undefined;
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
        if (this.app.appConfigurationSpec.configFileRepositoryRequired) {
            this.removeDefaultElement();
        } else {
            this.addDefaultElement();
            this.removeElementsFromUpdateConfig();
        }
    }

    public addDefaultElement(): void {
        let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, 'components', 0, 'components']);
        if (!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))) {
            config = config.find(val => val.key === 'configuration');
            config.components.length = 0;
            config.components.push(this.configTemplateService.getDefaultElement());
        }
    }

    public removeDefaultElement(): void {
        const config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, 'components', 0, 'components']);
        if (!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))) {
            config.find(val => val.key === 'configuration').components.length = 0;
        }
    }

    public removeElementsFromUpdateConfig(): void {
        if (!isNullOrUndefined(this.app.configUpdateWizardTemplate)) {
            const config = this.getNestedObject(this.app.configUpdateWizardTemplate.template, ['components']);
            if (!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))) {
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
        }, err => console.debug(err.message));
    }


    public getScreenshots(id: number): void {
        this.appImagesService.getAppScreenshotsUrls(id).subscribe(fileInfo => {
            fileInfo.forEach(val => {
                this.appImagesService.getAppScreenshotFile(id, val.id).subscribe(img => {
                    this.screenshots.push(this.convertToProperImageFile(img));
                }, err => console.debug(err.message));
            });
        }, err => console.debug(err.message));
    }

    private convertToProperImageFile(file: any) {
        const result: any = new File([file], 'uploaded file', {type: file.type});
        result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
        return result;
    }

    public addNewAccessMethod(): void {
        this.app.appDeploymentSpec.accessMethods.push(new AppAccessMethod())
    }

    public accessMethodTypeOptions(): string[] {
        const keys: Set<string> = new Set(Object.keys(ServiceAccessMethodType));
        if (this.app.appDeploymentSpec.accessMethods.
        find(p => ServiceAccessMethod.getServiceAccessMethodTypeAsEnum(p.type) === ServiceAccessMethodType.DEFAULT)) {
            keys.delete(ServiceAccessMethodType[ServiceAccessMethodType.DEFAULT]);
        }
        return Array.from(keys);
    }

    public removeAccessMethod(event): void {
        this.app.appDeploymentSpec.accessMethods.splice(event, 1);
    }

    public addNewStorageVolume(): void {
        this.app.appDeploymentSpec.storageVolumes.push(new AppStorageVolume())
    }

    public storageVolumeTypeOptions(): string[] {
        const keys: Set<string> = new Set(Object.keys(ServiceStorageVolumeType));
        if (this.app.appDeploymentSpec.storageVolumes.
        find(p => ServiceStorageVolume.getServiceStorageVolumeTypeAsEnum(p.type) === ServiceStorageVolumeType.MAIN)) {
            keys.delete(ServiceStorageVolumeType[ServiceStorageVolumeType.MAIN]);
        }
        return Array.from(keys);
    }

    public removeStorageVolume(event): void {
        this.app.appDeploymentSpec.storageVolumes.splice(event, 1);
    }

}
