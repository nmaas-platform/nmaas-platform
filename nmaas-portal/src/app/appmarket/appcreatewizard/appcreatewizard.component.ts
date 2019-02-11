import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {Application} from "../../model";
import {MenuItem, SelectItem} from "primeng/api";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {TagService} from "../../service";

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

  constructor(public fb:FormBuilder, public tagService: TagService) {
    this.basicAppInformationForm = this.fb.group({
      appName: ['', Validators.required],
      appVersion: ['', Validators.required],
      appLicense: ['', Validators.required],
      appLicenseUrl: ['', Validators.required],
      wwwUrl: ['', Validators.required],
      sourceUrl: ['', Validators.required],
      issuesUrl: ['', Validators.required],
      tags: ['', Validators.required]
    })
  }

  ngOnInit() {
    this.tagService.getTags().subscribe(tag => tag.forEach(val => {
      this.tags.push({label: val, value: val});
    }));
    this.steps = [
      {label: 'General information'},
      {label: 'Basic application information'},
      {label: 'Logo and screenshots'},
      {label: 'Deployment information'},
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

}
