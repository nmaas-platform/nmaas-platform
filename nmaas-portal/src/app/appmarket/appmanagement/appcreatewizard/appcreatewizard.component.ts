import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {Application, ConfigTemplate} from "../../../model";
import {MenuItem, SelectItem} from "primeng/api";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AppsService, TagService} from "../../../service";
import {AppDescription} from "../../../model/appdescription";
import {InternationalizationService} from "../../../service/internationalization.service";
import {isNullOrUndefined} from "util";
import {ConfigTemplateService} from "../../../service/configtemplate.service";
import {AppDeploymentSpec} from "../../../model/appdeploymentspec";
import {ParameterType} from "../../../model/parametertype";
import {ModalComponent} from "../../../shared/modal";
import {ApplicationState} from "../../../model/applicationstate";

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'app-appcreatewizard',
  templateUrl: './appcreatewizard.component.html',
  styleUrls: ['./appcreatewizard.component.css']
})

export class AppCreateWizardComponent implements OnInit {

  @ViewChild(ModalComponent)
  public modal:ModalComponent;

  public app:Application;
  public steps: MenuItem[];
  public activeStepIndex:number = 0;
  public basicAppInformationForm: FormGroup;
  public rulesAccepted: boolean = false;
  public tags: SelectItem[] = [];
  public logo: any ;
  public screenshots: any[] = [];
  public appDescriptions: AppDescription[] = [];
  public appDeploymentSpec: AppDeploymentSpec = new AppDeploymentSpec();
  public configUpdateTemplate: ConfigTemplate = new ConfigTemplate();
  public configTemplate: ConfigTemplate = new ConfigTemplate();

  constructor(public fb:FormBuilder, public tagService: TagService, public appsService: AppsService,
              public internationalization:InternationalizationService, public configTemplateService: ConfigTemplateService) {
    this.basicAppInformationForm = this.fb.group({
      name: ['', Validators.required],
      version: ['', Validators.required],
      license: ['', Validators.required],
      licenseUrl: ['', Validators.required],
      wwwUrl: ['', Validators.required],
      sourceUrl: ['', Validators.required],
      issuesUrl: ['', Validators.required],
      tags: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.internationalization.getAllSupportedLanguages().subscribe(val => {
      val.forEach(lang => {
        let appDescription:AppDescription = new AppDescription();
        appDescription.language = lang.language;
        this.appDescriptions.push(appDescription);
      });
    });
    this.tagService.getTags().subscribe(tag => tag.forEach(val => {
      this.tags.push({label: val, value: val});
    }));
    this.steps = [
      {label: 'General information'},
      {label: 'Basic application information'},
      {label: 'Logo and screenshots'},
      {label: 'Application descriptions'},
      {label: 'Configuration templates'},
      {label: 'Short review'}
    ];
    this.app = new Application();
  }

  public nextStep(): void{
    this.activeStepIndex += 1;
  }

  public previousStep(): void{
    this.activeStepIndex -= 1;
  }

  public submit(): void{
    this.setAppValues();
    this.appsService.addApp(this.app).subscribe(result => {
      this.appsService.uploadAppLogo(result.id, this.logo).subscribe(() => console.log("Logo uploaded"));
      for(let screenshot of this.screenshots){
        this.appsService.uploadScreenshot(result.id, screenshot).subscribe(() => console.log("Screenshot uploaded"));
      }
      this.modal.show();
    });
  }

  public setAppValues(): void {
    this.app = this.basicAppInformationForm.value;
    this.app.appDeploymentSpec = this.appDeploymentSpec;
    this.app.descriptions = this.appDescriptions;
    this.app.configTemplate = this.configTemplate;
    if(isNullOrUndefined(this.app.configTemplate.template) || this.app.configTemplate.template === ""){
      this.app.configTemplate.template = this.configTemplateService.getConfigTemplate();
    }
    if (!isNullOrUndefined(this.configUpdateTemplate.template) && this.configUpdateTemplate.template != "") {
      this.app.configurationUpdateTemplate = this.configUpdateTemplate;
    }
    this.app.state = ApplicationState.NEW;
  }

  public isValid(): boolean{
    return this.basicAppInformationForm.valid;
  }

  public changeRulesAcceptedFlag(): void {
    this.rulesAccepted = !this.rulesAccepted;
  }

  public clearLogo(event): void {
    this.logo = undefined;
  }

  public clearScreenshots(event): void {
    this.screenshots = this.screenshots.filter(val => val.name != event.file.name);
  }

  public getLogoUrl(event): void {
    let files = event.files;
    if(files[0].type.match(/image\/*/) != null){
      this.logo = files[0];
    }
  }

  public getScreenshotsUrl(event): void {
    let files = event.files;
    for(let file of files) {
      if(file.type.match(/image\/*/) != null){
        this.screenshots.push(file);
      }
    }
  }

  public isInvalidDescriptions(): boolean {
    let enAppDescription  = this.appDescriptions.filter(lang => lang.language === "en")[0];
    return isNullOrUndefined(enAppDescription.fullDescription) || enAppDescription.fullDescription === "" || isNullOrUndefined(enAppDescription.briefDescription) || enAppDescription.briefDescription === "";
  }

  public setConfigTemplate(event): void {
    this.configTemplate.template = event.form;
  }

  public setUpdateConfigTemplate(event): void {
    this.configUpdateTemplate.template = event.form;
  }

  public getParametersTypes(): string[] {
    return Object.keys(ParameterType).map(key => ParameterType[key]).filter(value => typeof value === 'string') as string[];
  }

  public addToDeployParametersMap(key:string, event){
    this.appDeploymentSpec.deployParameters.set(ParameterType[key], event.target.value);
  }

  public getDeployParameterValue(key:string) {
    return this.appDeploymentSpec.deployParameters.get(ParameterType[key]) || '';
  }

}
