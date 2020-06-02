import {Component, Input, OnInit} from '@angular/core';
import {AppDeploymentSpec} from '../../../model/appdeploymentspec';

@Component({
  selector: 'app-static-global-deploy-parameters-edit',
  templateUrl: './app-static-global-deploy-parameters-edit.component.html',
  styleUrls: ['./app-static-global-deploy-parameters-edit.component.css']
})
export class AppStaticGlobalDeployParametersEditComponent implements OnInit {

  @Input()
  public appDeploymentSpec: AppDeploymentSpec = undefined;

  public newKey = '';
  public newValue = '';

  public defaultTooltipOptions = {
    'placement': 'bottom',
    'show-delay': '50',
    'theme': 'dark'
  };

  constructor() { }

  ngOnInit() {
  }

  public isNewDeployParamValid(): boolean {
    if (!this.newKey || !this.newValue) {
      return false;
    }
    if (!!this.newKey && this.appDeploymentSpec.globalDeployParameters.hasOwnProperty(this.newKey)) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    if (this.isNewDeployParamValid()) {
      this.appDeploymentSpec.globalDeployParameters[this.newKey] = this.newValue;
      this.newKey = '';
      this.newValue = '';
    }
  }

  public removeDeployParam(key: string): void {
    if (this.appDeploymentSpec.globalDeployParameters.hasOwnProperty(key)) {
      delete this.appDeploymentSpec.globalDeployParameters[key];
    }
  }

  public getDeploymentParamsKeys(): string[] {
    return Object.keys(this.appDeploymentSpec.globalDeployParameters);
  }

}
