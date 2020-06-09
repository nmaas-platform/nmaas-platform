import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {AppDeploymentSpec} from '../../../model/appdeploymentspec';
import {FormControl, ValidatorFn} from '@angular/forms';

/**
 * This class is used to manage key value properties of `AppDeploymentSpec` class
 * Input Params:
 * - `propertyName` (string) - specify exact name of property that contains key-value pairs
 * - `(key|value)Validator` - custom validator
 * - `(key|value)ValidatorErrorKey` - error key(name) of custom validation
 * - `(key|value)ValidatorMessage` - message for errors during custom validation
 */
@Component({
  selector: 'app-static-global-deploy-parameters-edit',
  templateUrl: './app-static-global-deploy-parameters-edit.component.html',
  styleUrls: ['./app-static-global-deploy-parameters-edit.component.css']
})
export class AppStaticGlobalDeployParametersEditComponent implements OnInit, AfterViewInit {

  @Input()
  public appDeploymentSpec: AppDeploymentSpec = undefined;

  @Input()
  public propertyName: string = undefined;

  @Input()
  public keyValidator: ValidatorFn = undefined;

  @Input()
  public keyValidatorErrorKey: string = undefined;

  @Input()
  public keyValidatorMessage: string = undefined;

  @Input()
  public valueValidator: ValidatorFn = undefined;

  @Input()
  public valueValidatorErrorKey: string = undefined;

  @Input()
  public valueValidatorMessage: string = undefined;

  public newKeyFormControl: FormControl = undefined;
  public newValueFormControl: FormControl = undefined;

  public newKey = '';
  public newValue = '';

  public defaultTooltipOptions = {
    'placement': 'bottom',
    'show-delay': '50',
    'theme': 'dark'
  };

  constructor() { }

  ngOnInit() {
    this.newKeyFormControl = new FormControl('');
    this.newValueFormControl = new FormControl('');
    if (this.keyValidator) {
      this.newKeyFormControl.setValidators(this.keyValidator);
    }
    if (this.valueValidator) {
      this.newValueFormControl.setValidators(this.valueValidator);
    }
  }

  ngAfterViewInit(): void {}

  public isNewDeployParamValid(): boolean {
    this.newKey = this.newKeyFormControl.value;
    this.newValue = this.newValueFormControl.value;

    // form fields not valid
    if (!this.newKeyFormControl.valid || !this.newValueFormControl.valid) {
      return false;
    }

    if (!this.newKey || !this.newValue) {
      return false;
    }
    if (!!this.newKey && this.appDeploymentSpec[this.propertyName].hasOwnProperty(this.newKey)) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    this.newKey = this.newKeyFormControl.value;
    this.newValue = this.newValueFormControl.value;

    if (this.isNewDeployParamValid()) {
      this.appDeploymentSpec[this.propertyName][this.newKey] = this.newValue;
      this.newKeyFormControl = new FormControl('');
      this.newValueFormControl = new FormControl('');
      this.newKey = '';
      this.newValue = '';
    }
  }

  public removeDeployParam(key: string): void {
    this.newKey = this.newKeyFormControl.value;
    this.newValue = this.newValueFormControl.value;

    if (this.appDeploymentSpec[this.propertyName].hasOwnProperty(key)) {
      delete this.appDeploymentSpec[this.propertyName][key];
    }
  }

  public getDeploymentParamsKeys(): string[] {
    return Object.keys(this.appDeploymentSpec[this.propertyName]);
  }

}
