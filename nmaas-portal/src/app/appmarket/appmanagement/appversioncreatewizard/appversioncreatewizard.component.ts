import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from "../../../shared/common/basecomponent/base.component";
import {ModalComponent} from "../../../shared/modal";
import {MenuItem, MultiSelect, SelectItem} from "primeng/primeng";
import {Application, ConfigWizardTemplate} from "../../../model";
import {ConfigFileTemplate} from "../../../model/configfiletemplate";
import {AppImagesService, AppsService} from "../../../service";
import {ActivatedRoute, Router} from "@angular/router";
import {ConfigTemplateService} from "../../../service/configtemplate.service";
import {isNullOrUndefined} from "util";
import {ParameterType} from "../../../model/parametertype";
import {KubernetesTemplate} from "../../../model/kubernetestemplate";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer} from "@angular/platform-browser";
import {ApplicationState} from "../../../model/applicationstate";
import {KubernetesChart} from "../../../model/kuberneteschart";
import {AppAccessMethod} from "../../../model/app-access-method";
import {ServiceAccessMethodType} from "../../../model/serviceaccessmethod";

@Component({
    selector: 'app-appversioncreatewizard',
    templateUrl: './appversioncreatewizard.component.html',
    styleUrls: ['./appversioncreatewizard.component.css']
})
export class AppVersionCreateWizardComponent extends BaseComponent implements OnInit {


    @ViewChild(ModalComponent)
    public modal: ModalComponent;

    @ViewChild('tagsMultiSelect')
    public tagsMultiSelect: MultiSelect;

    public app: Application;
    public appName: string;
    public steps: MenuItem[];
    public activeStepIndex: number = 0;
    public rulesAccepted: boolean = false;
    public deployParameter: SelectItem[] = [];
    public selectedDeployParameters: string[] = [];
    public errorMessage: string = undefined;
    public configFileTemplates: ConfigFileTemplate[] = [];
    public addConfigUpdate: boolean = false;
    public basicAuth: boolean = false;
    public formDisplayChange: boolean = true;
    public logo: any[] = [];
    public screenshots: any[] = [];

    public defaultTooltipOptions = {
        'placement': 'right',
        'show-delay': "50",
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
        this.getParametersTypes().forEach(val => this.deployParameter.push({label: val.replace("_", " "), value: val}));
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

        let temp: Map<ParameterType, string> = new Map();
        Object.keys(appToEdit.appDeploymentSpec.deployParameters).forEach(key => {
            temp.set(ParameterType[key], appToEdit.appDeploymentSpec.deployParameters[key]);
            this.selectedDeployParameters.push(key);
        });
        this.app.appDeploymentSpec.deployParameters = temp;

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
        return this.configFileTemplates.length > 0 && !isNullOrUndefined(this.configFileTemplates[0].configFileName) && !isNullOrUndefined(this.configFileTemplates[0].configFileTemplateContent);
    }

    public isChartValid(): boolean {
        return !isNullOrUndefined(this.app.appDeploymentSpec.kubernetesTemplate.chart) && this.app.appDeploymentSpec.kubernetesTemplate.chart.name !== "" && this.app.appDeploymentSpec.kubernetesTemplate.chart.version !== "";
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

    public getParametersTypes(): string[] {
        return Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];
    }

    public addToDeployParametersMap(key: string, event) {
        this.app.appDeploymentSpec.deployParameters.set(ParameterType[key], event.target.value);
    }

    public getDeployParameterValue(key: string) {
        if (this.app.appDeploymentSpec.deployParameters instanceof Map) {
            return this.app.appDeploymentSpec.deployParameters.get(ParameterType[key]) || '';
        }
        return '';
    }

    public removeDeployParameterFromMap(event) {
        if (!event.value.some(val => val === event.itemValue)) {
            this.app.appDeploymentSpec.deployParameters.delete(ParameterType[event.itemValue as string]);
        }
    }

    public addConfig() {
        this.configFileTemplates.push(new ConfigFileTemplate());
    }

    public removeConfig(id: number) {
        this.configFileTemplates.splice(id, 1);
    }

    public hasAlreadyBasicAuth(): boolean {
        if (isNullOrUndefined(this.app.configWizardTemplate)) {
            return false;
        }
        let config: string = JSON.stringify(this.app.configWizardTemplate.template);
        return config.search(/accessCredentials/g) != -1 && config.search(/accessUsername/g) != -1 && config.search(/accessPassword/g) != -1;
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
        let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
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
        let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
        if (!isNullOrUndefined(config)) {
            let index = config.findIndex(val => val.key === 'accessCredentials');
            config.splice(index, 1);
        }
        this.app.configUpdateWizardTemplate.template.components = this.app.configUpdateWizardTemplate.template.components.filter(val => val.key != "accessCredentials");
        this.removeEmptyUpdateConfig();
    }

    public removeEmptyUpdateConfig(): void {
        let updateConfig = this.getNestedObject(this.app.configUpdateWizardTemplate.template, ["components", 0, "components"]);
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
        let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
        if (!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))) {
            config = config.find(val => val.key === 'configuration');
            config.components.length = 0;
            config.components.push(this.configTemplateService.getDefaultElement());
        }
    }

    public removeDefaultElement(): void {
        let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
        if (!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))) {
            config.find(val => val.key === 'configuration').components.length = 0;
        }
    }

    public removeElementsFromUpdateConfig(): void {
        if (!isNullOrUndefined(this.app.configUpdateWizardTemplate)) {
            let config = this.getNestedObject(this.app.configUpdateWizardTemplate.template, ["components"]);
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
        let result: any = new File([file], 'uploaded file', {type: file.type});
        result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
        return result;
    }

    private getServiceAccessMethodTypeAsEnum(arg: string | ServiceAccessMethodType): ServiceAccessMethodType {
        if(typeof arg === 'string') {
            return ServiceAccessMethodType[arg];
        }
        return arg;
    }

    private getDefaultAccessMethod(): AppAccessMethod {
        let result: AppAccessMethod = undefined;
        result = this.app.appDeploymentSpec.accessMethods.find(a => this.getServiceAccessMethodTypeAsEnum(a.type) === ServiceAccessMethodType.DEFAULT);
        return result;
    }

    private getInternalAccessMethods(): AppAccessMethod[] {
        return this.app.appDeploymentSpec.accessMethods.filter(a => this.getServiceAccessMethodTypeAsEnum(a.type) === ServiceAccessMethodType.INTERNAL);
    }

    private getExternalAccessMethods(): AppAccessMethod[] {
        return this.app.appDeploymentSpec.accessMethods.filter(a => this.getServiceAccessMethodTypeAsEnum(a.type) === ServiceAccessMethodType.EXTERNAL);
    }

    private getObjectKeys(arg: object): string[] {
        return Object.keys(arg);
    }
}
