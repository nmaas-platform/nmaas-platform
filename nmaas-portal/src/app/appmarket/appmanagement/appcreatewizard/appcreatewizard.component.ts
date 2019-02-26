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
  public steps: MenuItem[];
  public activeStepIndex:number = 0;
  public rulesAccepted: boolean = false;
  public tags: SelectItem[] = [];
  public logo: any[] = [];
  public screenshots: any[] = [];
  public errorMessage:string = undefined;

  constructor(public tagService: TagService, public appsService: AppsService, public route: ActivatedRoute,
              public internationalization:InternationalizationService, public configTemplateService: ConfigTemplateService,
              public appImagesService: AppImagesService, public router:Router, public translate: TranslateService) {
    super();
  }

  ngOnInit() {
    this.mode = this.getMode(this.route);
    this.tagService.getTags().subscribe(tag => tag.forEach(val => {
      this.tags.push({label: val, value: val});
    }));
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
    });
    this.app.appDeploymentSpec.deployParameters = temp;
    if(isNullOrUndefined(this.app.configTemplate)){
      this.app.configTemplate = new ConfigTemplate();
      this.app.configTemplate.template = this.configTemplateService.getConfigTemplate();
    }
    if(isNullOrUndefined(this.app.configurationUpdateTemplate)){
      this.app.configurationUpdateTemplate = new ConfigTemplate();
      this.app.configurationUpdateTemplate.template = this.configTemplateService.getConfigUpdateTemplate();
    }
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
    this.activeStepIndex -= 1;
  }

  public addApplication(): void{
    this.appsService.addApp(this.app).subscribe(result => {
      this.appsService.uploadAppLogo(result.id, this.logo).subscribe(() => console.log("Logo uploaded"));
      for(let screenshot of this.screenshots){
        this.appsService.uploadScreenshot(result.id, screenshot).subscribe(() => console.log("Screenshot uploaded"));
      }
      this.errorMessage = undefined;
      this.modal.show();
    }, error => this.errorMessage = error.message);
  }

  public updateApplication(): void {
    this.appsService.updateApp(this.app).subscribe(result => {
      this.errorMessage = undefined;
      this.router.navigate(['management/apps']);
    }, error => this.errorMessage = error.message);
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

  public getLogoUrl(event): void {
    let files = event.files;
    if(files[0].type.match(/image\/*/) != null){
      this.logo = files[0];
    }
  }

  public isInvalidDescriptions(): boolean {
    let enAppDescription  = this.app.descriptions.filter(lang => lang.language === "en")[0];
    return isNullOrUndefined(enAppDescription.fullDescription) || enAppDescription.fullDescription === "" || isNullOrUndefined(enAppDescription.briefDescription) || enAppDescription.briefDescription === "";
  }

  public setConfigTemplate(event): void {
    this.app.configTemplate.template = event.form;
  }

  public setUpdateConfigTemplate(event): void {
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

}
