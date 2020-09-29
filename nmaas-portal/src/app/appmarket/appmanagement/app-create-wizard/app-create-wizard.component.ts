import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {ConfigWizardTemplate} from '../../../model';
import {MenuItem, SelectItem} from 'primeng/api';
import {AppImagesService, AppsService, TagService} from '../../../service';
import {AppDescription} from '../../../model/app-description';
import {InternationalizationService} from '../../../service/internationalization.service';
import {ConfigTemplateService} from '../../../service/configtemplate.service';
import {ParameterType} from '../../../model/parametertype';
import {ModalComponent} from '../../../shared/modal';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {DomSanitizer} from '@angular/platform-browser';
import {ComponentMode} from '../../../shared';
import {MultiSelect} from 'primeng/multiselect';
import {ConfigFileTemplate} from '../../../model/configfiletemplate';
import {AppStorageVolume} from '../../../model/app-storage-volume';
import {parseServiceStorageVolumeType, ServiceStorageVolumeType} from '../../../model/service-storage-volume';
import {AppAccessMethod} from '../../../model/app-access-method';
import {parseServiceAccessMethodType, ServiceAccessMethodType} from '../../../model/service-access-method';
import {ValidatorFn} from '@angular/forms';
import {noParameterTypeInControlValueValidator} from '../app-version-create-wizard/appversioncreatewizard.component';
import {ApplicationDTO} from '../../../model/application-dto';
import {ApplicationBase} from '../../../model/application-base';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-appcreatewizard',
    templateUrl: './app-create-wizard.component.html',
    styleUrls: ['./app-create-wizard.component.css']
})

export class AppCreateWizardComponent extends BaseComponent implements OnInit {

    @ViewChild(ModalComponent, {static: true})
    public modal: ModalComponent;

    @ViewChild('tagsMultiSelect')
    public tagsMultiSelect: MultiSelect;

    public applicationDTO: ApplicationDTO;
    public appName: string;
    public steps: MenuItem[];
    public activeStepIndex = 0;
    public rulesAccepted = false;
    public tags: SelectItem[] = [];
    public newTags: string[] = [];
    public deployParameter: SelectItem[] = [];
    public selectedDeployParameters: string[] = [];
    public logo: any[] = [];
    public screenshots: any[] = [];
    public errorMessage: string = undefined;
    public urlPattern = '(http(s)?:\\/\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)';
    public configFileTemplates: ConfigFileTemplate[] = [];
    public addConfigUpdate = false;
    public basicAuth = false;
    public selectedLanguages: string[] = [];
    public languages: SelectItem[] = [];
    public formDisplayChange = true;

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

    constructor(public tagService: TagService,
                public appsService: AppsService,
                public route: ActivatedRoute,
                public internationalization: InternationalizationService,
                public configTemplateService: ConfigTemplateService,
                public appImagesService: AppImagesService,
                public router: Router,
                public translate: TranslateService,
                public dom: DomSanitizer) {
        super();
    }

    ngOnInit() {
        this.modal.setModalType('success');
        this.modal.setStatusOfIcons(false);
        this.mode = this.getMode(this.route);
        this.tagService.getTags().subscribe(tag => tag.forEach(val => {
            this.tags.push({
                label: val, value: {
                    id: null,
                    name: val
                }
            });
        }));
        this.getParametersTypes().forEach(val => this.deployParameter.push({label: val.replace('_', ' '), value: val}));
        this.steps = this.getSteps();
        this.route.params.subscribe(params => {
            if (params['id'] == null) {
                this.createNewWizard();
            } else {
                this.appsService.getApplicationBase(params['id']).subscribe(
                    result => {
                        this.applicationDTO = new ApplicationDTO()
                        this.applicationDTO.applicationBase = result;
                        this.appName = result.name;
                        this.fillWizardWithData(this.applicationDTO.applicationBase);
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

    public getSteps(): any {
        if (this.isInMode(ComponentMode.CREATE)) {
            return [
                {label: this.translate.instant('APPS_WIZARD.GENERAL_INFO_STEP')},
                {label: this.translate.instant('APPS_WIZARD.BASIC_APP_INFO_STEP')},
                {label: this.translate.instant('APPS_WIZARD.LOGO_AND_SCREENSHOTS_STEP')},
                {label: this.translate.instant('APPS_WIZARD.APP_DESCRIPTIONS_STEP')},
                {label: this.translate.instant('APPS_WIZARD.APP_DEPLOYMENT_SPEC_STEP')},
                {label: this.translate.instant('APPS_WIZARD.CONFIG_TEMPLATES_STEP')},
                {label: this.translate.instant('APPS_WIZARD.SHORT_REVIEW_STEP')}
            ];
        }
        return [
            {label: this.translate.instant('APPS_WIZARD.GENERAL_INFO_STEP')},
            {label: this.translate.instant('APPS_WIZARD.BASIC_APP_INFO_STEP')},
            {label: this.translate.instant('APPS_WIZARD.LOGO_AND_SCREENSHOTS_STEP')},
            {label: this.translate.instant('APPS_WIZARD.APP_DESCRIPTIONS_STEP')},
            {label: this.translate.instant('APPS_WIZARD.SHORT_REVIEW_STEP')}
        ];
    }

    public fillWizardWithData(appToEdit: ApplicationBase): void {
        this.getLogo(appToEdit.id);
        this.getScreenshots(appToEdit.id);
        this.applicationDTO.applicationBase.tags.forEach(appTag => {
            if (!this.tags.some(tag => tag.value === appTag)) {
                this.tags.push({label: appTag.name, value: appTag});
            }
        });
        this.internationalization.getAllSupportedLanguages().subscribe(
            val => val.filter(lang => lang.language !== 'en').forEach(lang => this.languages.push({
                label: this.translate.instant('LANGUAGE.' + lang.language.toUpperCase() + '_LABEL'),
                value: lang.language
            }))
        );
    }

    public getLogo(id: number): void {
        this.appImagesService.getLogoFile(id).subscribe(
            file => {
                this.logo.push(this.convertToProperImageFile(file));
            },
            err => console.error(err.message)
        );
    }

    public getScreenshots(id: number): void {
        this.appImagesService.getAppScreenshotsUrls(id).subscribe(
            fileInfo => {
                fileInfo.forEach(
                    val => {
                        this.appImagesService.getAppScreenshotFile(id, val.id).subscribe(
                            img => {
                                this.screenshots.push(this.convertToProperImageFile(img));
                            },
                            err => console.error(err.message));
                    });
            },
            err => console.error(err.message)
        );
    }

    private convertToProperImageFile(file: any) {
        const result: any = new File([file], 'uploaded file', {type: file.type});
        result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
        return result;
    }

    public createNewWizard(): void {
        this.applicationDTO = new ApplicationDTO();
        this.internationalization.getAllSupportedLanguages().subscribe(val => {
            val.forEach(lang => {
                const appDescription: AppDescription = new AppDescription();
                appDescription.language = lang.language;
                this.applicationDTO.applicationBase.descriptions.push(appDescription);
                if (lang.language !== 'en') {
                    this.languages.push({
                        label: this.translate.instant('LANGUAGE.' + lang.language.toUpperCase() + '_LABEL'),
                        value: lang.language
                    });
                }
            });
        });
        this.configFileTemplates.push(new ConfigFileTemplate());
        this.applicationDTO.application.configWizardTemplate = new ConfigWizardTemplate();
        this.applicationDTO.application.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
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
        // rewrite name
        this.applicationDTO.application.name = this.applicationDTO.applicationBase.name;
        this.appsService.createApplicationDTO(this.applicationDTO).subscribe(
            result => {
                this.uploadLogo(result.id);
                this.handleUploadingScreenshots(result.id);
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
        this.appsService.updateApplicationBase(this.applicationDTO.applicationBase).subscribe(
            () => {
                this.uploadLogo(this.applicationDTO.applicationBase.id);
                this.handleUploadingScreenshots(this.applicationDTO.applicationBase.id);
                this.errorMessage = undefined;
                this.modal.show();
            },
            error => this.errorMessage = error.message
        );
    }

    public templateHasContent(): boolean {
        return this.configFileTemplates.length > 0
            && this.configFileTemplates[0].configFileName != null
            && this.configFileTemplates[0].configFileTemplateContent != null;
    }

    public uploadLogo(id: number) {
        if (this.isInMode(ComponentMode.EDIT) && this.logo[0] == null) {
            this.appImagesService.deleteLogo(id).subscribe(() => console.debug('Logo deleted'));
        }
        if (this.logo[0] != null) {
            this.appsService.uploadAppLogo(id, this.logo[0]).subscribe(() => console.debug('Logo uploaded'));
        }
    }

    public handleUploadingScreenshots(id: number) {
        if (this.isInMode(ComponentMode.EDIT)) {
            this.appImagesService.deleteScreenshots(id).subscribe(() => {
                this.uploadScreenshots(id);
            });
        } else {
            this.uploadScreenshots(id);
        }
    }

    private uploadScreenshots(id: number) {
        for (const screenshot of this.screenshots) {
            this.appsService.uploadScreenshot(id, screenshot).subscribe(() => console.debug('Screenshot uploaded'));
        }
    }

    public changeRulesAcceptedFlag(): void {
        this.rulesAccepted = !this.rulesAccepted;
    }

    public onSelectLogo(event): void {
        this.logo.push(...event.files)
    }

    public clearLogo(event): void {
        this.logo = [];
    }

    public canAddLogo(): boolean {
        return this.logo.length > 0;
    }

    public onSelectScreenshots(event) {
        this.screenshots.push(...event.files)
    }

    public onRemoveScreenshot(event) {
        const index = this.screenshots.indexOf(event.file)
        this.screenshots.splice(index, 1)
    }

    public isInvalidDescriptions(): boolean {
        const enAppDescription = this.applicationDTO.applicationBase.descriptions.filter(lang => lang.language === 'en')[0];
        return enAppDescription.fullDescription == null
            || enAppDescription.fullDescription === ''
            || enAppDescription.briefDescription == null
            || enAppDescription.briefDescription === '';
    }

    public getDescriptionsInSelectedLanguage(lang: string): AppDescription {
        return this.applicationDTO.applicationBase.descriptions
            .filter(description => description.language === lang)[0] || this.createAppDescription(lang);
    }

    public createAppDescription(lang: string): AppDescription {
        const description: AppDescription = new AppDescription();
        description.language = lang;
        return description;
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

    public getParametersTypes(): string[] {
        return Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];
    }

    public addToDeployParametersMap(key: string, event) {
        this.applicationDTO.application.appDeploymentSpec.deployParameters[key] = event.target.value;
    }

    public getDeployParameterValue(key: string) {
        return this.applicationDTO.application.appDeploymentSpec.deployParameters[key] || '';
    }

    public removeDeployParameterFromMap(event) {
        if (!event.value.some(val => val === event.itemValue)) {
            delete this.applicationDTO.application.appDeploymentSpec.deployParameters[event.itemValue as string];
        }
    }

    public addNewTag(event) {
        if (!this.applicationDTO.applicationBase.tags.some(tag => tag.name.toLowerCase() === event.value.toLowerCase())) {
            this.applicationDTO.applicationBase.tags.push({
                id: null,
                name: event.value.toLowerCase()
            });
        }
        if (!this.tags.some(tag => tag.value.name.toLowerCase() === event.value.toLowerCase())) {
            this.tags.push({
                label: event.value, value: {
                    id: null,
                    name: event.value.toLowerCase()
                }
            });
        } else {
            this.newTags.pop()
        }
        this.tagsMultiSelect.ngOnInit();
    }

    public removeNewTag(event) {
        this.tags = this.tags.filter(tag => tag.value !== event.value);
        this.applicationDTO.applicationBase.tags = this.applicationDTO.applicationBase.tags.filter(tag => tag !== event.value);
    }

    public addConfig() {
        this.configFileTemplates.push(new ConfigFileTemplate());
    }

    public removeConfig(id: number) {
        this.configFileTemplates.splice(id, 1);
    }

    /**
     * checks if form has basic auth params
     */
    public hasAlreadyBasicAuth(): boolean {
        if (this.applicationDTO.application.configWizardTemplate == null) {
            return false;
        }
        const config: string = JSON.stringify(this.applicationDTO.application.configWizardTemplate.template);
        return config.search(/accessCredentials/g) !== -1
            && config.search(/accessUsername/g) !== -1
            && config.search(/accessPassword/g) !== -1;
    }

    public handleBasicAuth() {
        if (!this.applicationDTO.application.appConfigurationSpec.configFileRepositoryRequired
            && this.applicationDTO.application.configWizardTemplate == null) {
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
        const updateConfig = this.getNestedObject(this.applicationDTO.application.configUpdateWizardTemplate.template, ['components', 0, 'components']);
        if (updateConfig == null || updateConfig.length === 0) {
            this.applicationDTO.application.configUpdateWizardTemplate = undefined;
            this.addConfigUpdate = false;
        }
    }

    public handleConfigTemplate(): any {
        if (this.addConfigUpdate && this.applicationDTO.application.configUpdateWizardTemplate == null) {
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
        if (config != null && config.find(val => val.key === 'configuration') != null) {
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
        if (config != null && config.find(val => val.key === 'configuration') != null) {
            config.find(val => val.key === 'configuration').components.length = 0;
        }
    }

    public removeElementsFromUpdateConfig(): void {
        if (this.applicationDTO.application.configUpdateWizardTemplate != null) {
            const config = this.getNestedObject(this.applicationDTO.application.configUpdateWizardTemplate.template, ['components']);
            if (config != null && config.find(val => val.key === 'configuration') != null) {
                config.find(val => val.key === 'configuration').components.length = 0;
            }
            this.removeEmptyUpdateConfig();
        }
    }

    getNestedObject = (nestedObj, pathArr) => {
        return pathArr.reduce((obj, key) =>
            (obj && obj[key] !== 'undefined') ? obj[key] : undefined, nestedObj);
    };

    /**
     * Add new empty app access method to list
     */
    public addNewAccessMethod(): void {
        this.applicationDTO.application.appDeploymentSpec.accessMethods.push(new AppAccessMethod())
    }

    /**
     * returns list of available access method types
     * Only one DEFAULT access method is possible,
     * so this function returns all possible types but DEFAULT if default is currently being used
     */
    public accessMethodTypeOptions(): string[] {
        const keys: Set<string> = new Set(Object.keys(ServiceAccessMethodType));
        if (this.applicationDTO.application.appDeploymentSpec.accessMethods
            .find(p => parseServiceAccessMethodType(p.type) === ServiceAccessMethodType.DEFAULT)
        ) {
            keys.delete(ServiceAccessMethodType[ServiceAccessMethodType.DEFAULT]);
        }
        return Array.from(keys);
    }

    /**
     * remove app access method from list
     * @param event - id of an element to be removed
     */
    public removeAccessMethod(event): void {
        this.applicationDTO.application.appDeploymentSpec.accessMethods.splice(event, 1);
    }

    /**
     * Add new empty storage volume to list
     */
    public addNewStorageVolume(): void {
        this.applicationDTO.application.appDeploymentSpec.storageVolumes.push(new AppStorageVolume())
    }

    /**
     * get available storage volume types
     */
    public storageVolumeTypeOptions(): string[] {
        const keys: Set<string> = new Set(Object.keys(ServiceStorageVolumeType));
        if (this.applicationDTO.application.appDeploymentSpec.storageVolumes
            .find(p => parseServiceStorageVolumeType(p.type) === ServiceStorageVolumeType.MAIN)
        ) {
            keys.delete(ServiceStorageVolumeType[ServiceStorageVolumeType.MAIN]);
        }
        return Array.from(keys);
    }

    /**
     * remove storage volume from list
     * @param event - index of element to be removed
     */
    public removeStorageVolume(event): void {
        this.applicationDTO.application.appDeploymentSpec.storageVolumes.splice(event, 1);
    }

}
