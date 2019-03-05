import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {Application, ConfigTemplate} from "../../../model";
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

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'app-appcreatewizard',
  templateUrl: './appcreatewizard.component.html',
  styleUrls: ['./appcreatewizard.component.css']
})

export class AppCreateWizardComponent extends BaseComponent implements OnInit {

  @ViewChild(ModalComponent)
  public modal:ModalComponent;

  public app:Application;
  public appName: string;
  public steps: MenuItem[];
  public activeStepIndex:number = 0;
  public rulesAccepted: boolean = false;
  public tags: SelectItem[] = [];
  public deployParameter:SelectItem[] = [];
  public selectedDeployParameters: string[] = [];
  public logo: any[] = [];
  public screenshots: any[] = [];
  public errorMessage:string = undefined;
  public urlPattern: string = '(http(s)?:\\/\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)';

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
    this.steps = [
      {label: this.translate.instant('APPS_WIZARD.GENERAL_INFO_STEP')},
      {label: this.translate.instant('APPS_WIZARD.BASIC_APP_INFO_STEP')},
      {label: this.translate.instant('APPS_WIZARD.LOGO_AND_SCREENSHOTS_STEP')},
      {label: this.translate.instant('APPS_WIZARD.APP_DESCRIPTIONS_STEP')},
      {label: this.translate.instant('APPS_WIZARD.CONFIG_TEMPLATES_STEP')},
      {label: this.translate.instant('APPS_WIZARD.SHORT_REVIEW_STEP')}
    ];
    this.route.params.subscribe(params => {
      if(isNullOrUndefined(params['id'])){
        this.createNewWizard();
      } else {
        this.appsService.getApp(params['id']).subscribe(result =>{
            this.app = result;
            this.appName = result.name;
            this.fillWizardWithData(result);
        });
        this.rulesAccepted = true;
        this.activeStepIndex = 1;
      }
    });
  }

  public fillWizardWithData(appToEdit: Application): void {
    let temp:Map<ParameterType, string> = new Map();
    Object.keys(appToEdit.appDeploymentSpec.deployParameters).forEach(key =>{
      temp.set(ParameterType[key], appToEdit.appDeploymentSpec.deployParameters[key]);
      this.selectedDeployParameters.push(key);
    });
    this.app.appDeploymentSpec.deployParameters = temp;
    if(isNullOrUndefined(this.app.configTemplate)){
      this.app.configTemplate = new ConfigTemplate();
      this.app.configTemplate.template = this.configTemplateService.getConfigTemplate();
    }
    this.getLogo(appToEdit.id);
    this.getScreenshots(appToEdit.id);
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
      });
    });
  }

  public nextStep(): void{
    this.activeStepIndex += 1;
  }

  public previousStep(): void{
    this.errorMessage = undefined;
    this.activeStepIndex -= 1;
  }

  public addApplication(): void{
    this.appsService.addApp(this.app).subscribe(result => {
      this.uploadLogo(result.id);
      this.handleUploadingScreenshots(result.id);
      this.errorMessage = undefined;
      this.modal.show();
    }, error => this.errorMessage = error.message);
  }

  public updateApplication(): void {
    this.appsService.updateApp(this.app).subscribe(() => {
      this.uploadLogo(this.app.id);
      this.handleUploadingScreenshots(this.app.id);
      this.errorMessage = undefined;
      this.modal.show();
    }, error => this.errorMessage = error.message);
  }

  public uploadLogo(id: number){
    if(this.isInMode(ComponentMode.EDIT) && isNullOrUndefined(this.logo[0])){
      this.appImagesService.deleteLogo(id).subscribe(() => console.debug("Logo deleted"));
    }
    this.appsService.uploadAppLogo(id, this.logo[0]).subscribe(() => console.debug("Logo uploaded"));
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

  public setConfigTemplate(event): void {
    if(!this.app.configTemplate){
      this.app.configTemplate = new ConfigTemplate();
    }
    this.app.configTemplate.template = event.form;
  }

  public setUpdateConfigTemplate(event): void {
    if(!this.app.configurationUpdateTemplate){
      this.app.configurationUpdateTemplate = new ConfigTemplate();
    }
    this.app.configurationUpdateTemplate.template = event.form;
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

}
