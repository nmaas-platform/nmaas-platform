import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {Application} from "../../model";
import {MenuItem, SelectItem} from "primeng/api";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AppsService, TagService} from "../../service";
import {AppDescription} from "../../model/appdescription";
import {InternationalizationService} from "../../service/internationalization.service";
import {isNullOrUndefined} from "util";

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'app-appcreatewizard',
  templateUrl: './appcreatewizard.component.html',
  styleUrls: ['./appcreatewizard.component.css']
})

export class AppCreateWizardComponent implements OnInit {

  public app:Application;
  public steps: MenuItem[];
  public activeStepIndex:number = 0;
  public basicAppInformationForm: FormGroup;
  public rulesAccepted: boolean = false;
  public tags: SelectItem[] = [];
  public logo: any ;
  public screenshots: any[] = [];
  public appDescriptions: AppDescription[] = [];

  constructor(public fb:FormBuilder, public tagService: TagService, public appsService: AppsService, public internationalization:InternationalizationService) {
    this.basicAppInformationForm = this.fb.group({
      appName: ['', Validators.required],
      appVersion: ['', Validators.required],
      appLicense: ['', Validators.required],
      appLicenseUrl: ['', Validators.required],
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
    this.app = this.basicAppInformationForm.value;
  }

  public previousStep(): void{
    this.activeStepIndex -= 1;
  }

  public cancelButton(): void{

  }

  public submit(): void{

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

  public checkDescriptions(): boolean {
    let enAppDescription  = this.appDescriptions.filter(lang => lang.language === "en")[0];
    return isNullOrUndefined(enAppDescription.fullDescription) || enAppDescription.fullDescription === "" || isNullOrUndefined(enAppDescription.briefDescription) || enAppDescription.briefDescription === "";
  }

}
