import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {Application, ConfigWizardTemplate} from "../../../model";
import {MenuItem, SelectItem} from "primeng/api";
import {AppImagesService, AppsService, TagService} from "../../../service";
import {AppDescription} from "../../../model/appdescription";
import {InternationalizationService} from "../../../service/internationalization.service";
import {isNullOrUndefined} from "util";
import {ConfigTemplateService} from "../../../service/configtemplate.service";
import {ParameterType} from "../../../model/parametertype";
import {ModalComponent} from "../../../shared/modal";
import {BaseComponent} from "../../../shared/common/basecomponent/base.component";
import {ActivatedRoute, Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer} from "@angular/platform-browser";
import {ComponentMode} from "../../../shared";
import {MultiSelect} from "primeng/primeng";
import {KubernetesTemplate} from "../../../model/kubernetestemplate";
import {ConfigFileTemplate} from "../../../model/configfiletemplate";

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'app-appcreatewizard',
  templateUrl: './appcreatewizard.component.html',
  styleUrls: ['./appcreatewizard.component.css']
})

export class AppCreateWizardComponent extends BaseComponent implements OnInit {

  @ViewChild(ModalComponent)
  public modal:ModalComponent;

  @ViewChild('tagsMultiSelect')
  public tagsMultiSelect:MultiSelect;

  public app:Application;
  public appName: string;
  public steps: MenuItem[];
  public activeStepIndex:number = 0;
  public rulesAccepted: boolean = false;
  public tags: SelectItem[] = [];
  public newTags: string[] = [];
  public deployParameter:SelectItem[] = [];
  public selectedDeployParameters: string[] = [];
  public logo: any[] = [];
  public screenshots: any[] = [];
  public errorMessage:string = undefined;
  public urlPattern: string = '(http(s)?:\\/\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)';
  public configFileTemplates: ConfigFileTemplate[] = [];
  public addConfigUpdate: boolean = false;
  public basicAuth: boolean = false;
  public selectedLanguages: string[] = [];
  public languages: SelectItem[] = [];
  public formDisplayChange: boolean = true;

  public defaultTooltipOptions = {
      'placement': 'right',
      'show-delay': "50",
      'theme': 'dark'
  };

  constructor(public tagService: TagService, public appsService: AppsService, public route: ActivatedRoute,
              public internationalization:InternationalizationService, public configTemplateService: ConfigTemplateService,
              public appImagesService: AppImagesService, public router:Router, public translate: TranslateService,
              public dom:DomSanitizer) {
    super();
  }

  ngOnInit() {
    this.modal.setModalType('success');
    this.modal.setStatusOfIcons(false);
    this.mode = this.getMode(this.route);
    this.tagService.getTags().subscribe(tag => tag.forEach(val => {
      this.tags.push({label: val, value: val});
    }));
    this.getParametersTypes().forEach(val => this.deployParameter.push({label: val.replace("_", " "), value:val}));
    this.steps = this.getSteps()
    this.route.params.subscribe(params => {
      if(isNullOrUndefined(params['id'])){
        this.createNewWizard();
      } else {
        this.appsService.getBaseApp(params['id']).subscribe(result =>{
            this.app = result;
            this.appName = result.name;
            this.fillWizardWithData(result);
        });
        this.rulesAccepted = true;
        this.activeStepIndex = 1;
      }
    });
  }

  public getSteps() : any {
    if(this.isInMode(ComponentMode.CREATE)){
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

  public fillWizardWithData(appToEdit: Application): void {
    this.getLogo(appToEdit.id);
    this.getScreenshots(appToEdit.id);
    this.app.tags.forEach(appTag =>{
      if(!this.tags.some(tag => tag.value === appTag)){
        this.tags.push({label:appTag, value: appTag});
      }
    });
    this.internationalization.getAllSupportedLanguages().subscribe(val => val.filter(lang => lang.language != "en").forEach(lang => this.languages.push({label: this.translate.instant('LANGUAGE.' + lang.language.toUpperCase() + '_LABEL'), value: lang.language})));
  }

  public getLogo(id:number) : void {
    this.appImagesService.getLogoFile(id).subscribe(file => {
      this.logo.push(this.convertToProperImageFile(file));
    }, err => console.debug(err.message));
  }

  public getScreenshots(id:number): void {
    this.appImagesService.getAppScreenshotsUrls(id).subscribe(fileInfo => {
      fileInfo.forEach(val =>{
        this.appImagesService.getAppScreenshotFile(id, val.id).subscribe(img =>{
          this.screenshots.push(this.convertToProperImageFile(img));
        }, err => console.debug(err.message));
      });
    }, err => console.debug(err.message));
  }

  private convertToProperImageFile(file:any){
    let result: any = new File([file], 'uploaded file', {type: file.type});
    result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
    return result;
  }

  public createNewWizard() : void {
    this.app = new Application();
    this.internationalization.getAllSupportedLanguages().subscribe(val => {
      val.forEach(lang => {
        let appDescription:AppDescription = new AppDescription();
        appDescription.language = lang.language;
        this.app.descriptions.push(appDescription);
        if(lang.language != "en"){
          this.languages.push({label: this.translate.instant('LANGUAGE.' + lang.language.toUpperCase() + '_LABEL'), value: lang.language});
        }
      });
    });
    this.configFileTemplates.push(new ConfigFileTemplate());
    this.app.configWizardTemplate = new ConfigWizardTemplate();
    this.app.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
  }

  public nextStep(): void{
    this.activeStepIndex += 1;
  }

  public previousStep(): void{
    this.errorMessage = undefined;
    this.activeStepIndex -= 1;
  }

  public addApplication(): void {
    if(this.templateHasContent()){
      this.app.appConfigurationSpec.templates = this.configFileTemplates;
    }
    this.appsService.addApp(this.app).subscribe(result => {
      this.uploadLogo(result.id);
      this.handleUploadingScreenshots(result.id);
      this.errorMessage = undefined;
      this.modal.show();
    }, error => this.errorMessage = error.message);
  }

  public updateApplication(): void {
    if(this.templateHasContent()){
      this.app.appConfigurationSpec.templates = this.configFileTemplates;
    }
    this.appsService.updateBaseApp(this.app).subscribe(() => {
      this.uploadLogo(this.app.id);
      this.handleUploadingScreenshots(this.app.id);
      this.errorMessage = undefined;
      this.modal.show();
    }, error => this.errorMessage = error.message);
  }

  public templateHasContent() : boolean {
    return this.configFileTemplates.length > 0 && !isNullOrUndefined(this.configFileTemplates[0].configFileName) && !isNullOrUndefined(this.configFileTemplates[0].configFileTemplateContent);
  }

  public uploadLogo(id: number){
    if(this.isInMode(ComponentMode.EDIT) && isNullOrUndefined(this.logo[0])){
      this.appImagesService.deleteLogo(id).subscribe(() => console.debug("Logo deleted"));
    }
    if(!isNullOrUndefined(this.logo[0])){
      this.appsService.uploadAppLogo(id, this.logo[0]).subscribe(() => console.debug("Logo uploaded"));
    }
  }

  public handleUploadingScreenshots(id: number){
    if(this.isInMode(ComponentMode.EDIT)){
      this.appImagesService.deleteScreenshots(id).subscribe(()=>{
        this.uploadScreenshots(id);
      });
    } else {
      this.uploadScreenshots(id);
    }
  }

  private uploadScreenshots(id: number){
    for(let screenshot of this.screenshots){
      this.appsService.uploadScreenshot(id, screenshot).subscribe(() => console.debug("Screenshot uploaded"));
    }
  }

  public changeRulesAcceptedFlag(): void {
    this.rulesAccepted = !this.rulesAccepted;
  }

  public clearLogo(event): void {
    this.logo = [];
  }

  public canAddLogo(): boolean {
    return this.logo.length > 0;
  }

  public isInvalidDescriptions(): boolean {
    let enAppDescription  = this.app.descriptions.filter(lang => lang.language === "en")[0];
    return isNullOrUndefined(enAppDescription.fullDescription) || enAppDescription.fullDescription === "" || isNullOrUndefined(enAppDescription.briefDescription) || enAppDescription.briefDescription === "";
  }

  public getDescriptionsInSelectedLanguage(lang: string) : AppDescription {
    return this.app.descriptions.filter(description => description.language === lang)[0] || this.createAppDescription(lang);
  }

  public createAppDescription(lang: string): AppDescription {
    let description: AppDescription = new AppDescription();
    description.language = lang;
    return description;
  }

  public setConfigTemplate(event): void {
    if(!this.app.configWizardTemplate){
      this.app.configWizardTemplate = new ConfigWizardTemplate();
    }
    this.app.configWizardTemplate.template = event.form;
  }

  public setUpdateConfigTemplate(event): void {
    if(!this.app.configUpdateWizardTemplate){
      this.app.configUpdateWizardTemplate = new ConfigWizardTemplate();
    }
    this.app.configUpdateWizardTemplate.template = event.form;
  }

  public getParametersTypes(): string[] {
    return Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];
  }

  public addToDeployParametersMap(key:string, event){
    this.app.appDeploymentSpec.deployParameters.set(ParameterType[key], event.target.value);
  }

  public getDeployParameterValue(key:string) {
    if(this.app.appDeploymentSpec.deployParameters instanceof Map){
      return this.app.appDeploymentSpec.deployParameters.get(ParameterType[key]) || '';
    }
    return '';
  }

  public removeDeployParameterFromMap(event){
    if(!event.value.some(val => val === event.itemValue)){
      this.app.appDeploymentSpec.deployParameters.delete(ParameterType[event.itemValue as string]);
    }
  }

  public addNewTag(event){
    if(!this.app.tags.some(tag => tag.toLowerCase() === event.value.toLowerCase())){
      this.app.tags.push(event.value.toLowerCase());
    }
    if(!this.tags.some(tag => tag.value.toLowerCase() === event.value.toLowerCase())){
      this.tags.push({label: event.value, value: event.value.toLowerCase()});
    } else {
      this.newTags.pop()
    }
    this.tagsMultiSelect.ngOnInit();
  }

  public removeNewTag(event){
    this.tags = this.tags.filter(tag => tag.value != event.value);
    this.app.tags = this.app.tags.filter(tag => tag != event.value);
  }

  public addConfig(){
    this.configFileTemplates.push(new ConfigFileTemplate());
  }

  public removeConfig(id:number){
    this.configFileTemplates.splice(id, 1);
  }

  public hasAlreadyBasicAuth() : boolean {
    if(isNullOrUndefined(this.app.configWizardTemplate)){
      return false;
    }
    let config: string = JSON.stringify(this.app.configWizardTemplate.template);
    return config.search(/accessCredentials/g) != -1 && config.search(/accessUsername/g) != -1 && config.search(/accessPassword/g) != -1;
  }

  public handleBasicAuth(){
    if(!this.app.appConfigurationSpec.configFileRepositoryRequired && isNullOrUndefined(this.app.configWizardTemplate)){
      this.app.configWizardTemplate = new ConfigWizardTemplate();
      this.app.configWizardTemplate.template = this.configTemplateService.getConfigTemplate();
    }
    if(this.basicAuth){
      this.addBasicAuth();
    } else {
      this.removeBasicAuth();
    }
  }

  public addBasicAuth() : any {
    let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
    if(!isNullOrUndefined(config)){
      config.unshift(this.configTemplateService.getBasicAuth(this.app.name));
    }
    if(isNullOrUndefined(this.app.configUpdateWizardTemplate)){
      this.app.configUpdateWizardTemplate = new ConfigWizardTemplate();
      this.app.configUpdateWizardTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
    }
    this.app.configUpdateWizardTemplate.template.components.unshift(this.configTemplateService.getBasicAuth(this.app.name));
  }

  public removeBasicAuth() : any {
    let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
    if(!isNullOrUndefined(config)){
      let index = config.findIndex(val => val.key === 'accessCredentials');
      config.splice(index, 1);
    }
    this.app.configUpdateWizardTemplate.template.components = this.app.configUpdateWizardTemplate.template.components.filter(val => val.key != "accessCredentials");
    this.removeEmptyUpdateConfig();
  }

  public removeEmptyUpdateConfig() : void {
    let updateConfig = this.getNestedObject(this.app.configUpdateWizardTemplate.template, ["components", 0, "components"]);
    if(isNullOrUndefined(updateConfig) || updateConfig.length === 0){
      this.app.configUpdateWizardTemplate = undefined;
      this.addConfigUpdate = false;
    }
  }

  public handleConfigTemplate() : any {
    if(this.addConfigUpdate && isNullOrUndefined(this.app.configUpdateWizardTemplate)){
      this.app.configUpdateWizardTemplate = new ConfigWizardTemplate();
      this.app.configUpdateWizardTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
    }
    if(!this.addConfigUpdate && !this.hasAlreadyBasicAuth()){
      this.app.configUpdateWizardTemplate = undefined;
    }
  }

  public changeBasicAuthInForms() {
    this.formDisplayChange = false;
    this.handleBasicAuth();
    setTimeout(()=>{this.formDisplayChange = true},1);
  }

  public changeDefaultElementInForms() {
    this.formDisplayChange = false;
    this.handleDefaultElement();
    setTimeout(()=>{this.formDisplayChange = true},1);
  }

  public handleDefaultElement() {
    if(this.app.appConfigurationSpec.configFileRepositoryRequired) {
      this.removeDefaultElement();
    } else {
      this.addDefaultElement();
      this.removeElementsFromUpdateConfig();
    }
  }

  public addDefaultElement() : void {
    let config = this.getNestedObject(this.app.configWizardTemplate.template, ['components', 0, "components", 0, "components"]);
    if(!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))){
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

  public removeElementsFromUpdateConfig() : void {
    if(!isNullOrUndefined(this.app.configUpdateWizardTemplate)){
      let config = this.getNestedObject(this.app.configUpdateWizardTemplate.template, ["components"]);
      if(!isNullOrUndefined(config) && !isNullOrUndefined(config.find(val => val.key === 'configuration'))) {
        config.find(val => val.key === 'configuration').components.length = 0;
      }
      this.removeEmptyUpdateConfig();
    }
  }

  getNestedObject = (nestedObj, pathArr) => {
    return pathArr.reduce((obj, key) =>
        (obj && obj[key] !== 'undefined') ? obj[key] : undefined, nestedObj);
  };
}
