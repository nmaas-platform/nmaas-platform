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

  public globalDeployParamsMap: Map<string, string> = new Map<string, string>();

  public newKey = '';
  public newValue = '';

  public defaultTooltipOptions = {
    'placement': 'bottom',
    'show-delay': '50',
    'theme': 'dark'
  };

  private static convertObjectToStringMap(arg: object): Map<string, string> {
    const result = new Map<string, string>();
    for (const k of Object.keys(arg)) {
      if (!result.has(k) && typeof arg[k] === 'string') {
        result.set(k, arg[k]);
      }
    }
    return result;
  }

  private static convertStringMapToObject(arg: Map<string, string>): object {
    const result = {};
    for (const k of arg.keys()) {
      result[k] = arg.get(k);
    }
    return result;
  }

  constructor() { }

  ngOnInit() {
    this.globalDeployParamsMap =
        AppStaticGlobalDeployParametersEditComponent.convertObjectToStringMap(this.appDeploymentSpec.globalDeployParameters)
  }

  public isNewDeployParamValid(): boolean {
    if (!this.newKey || !this.newValue) {
      return false;
    }
    if (!!this.newKey && this.globalDeployParamsMap.has(this.newKey)) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    if (this.isNewDeployParamValid()) {
      this.globalDeployParamsMap.set(this.newKey, this.newValue);
      this.newKey = '';
      this.newValue = '';
      this.updateDeployParams();
    }
  }

  public removeDeployParam(key: string): void {
    if (this.globalDeployParamsMap.has(key)) {
      this.globalDeployParamsMap.delete(key);
      this.updateDeployParams();
    }
  }

  public updateDeployParams(): void {
    this.appDeploymentSpec.globalDeployParameters =
        AppStaticGlobalDeployParametersEditComponent.convertStringMapToObject(this.globalDeployParamsMap);
  }

  public reject(): void {
    this.globalDeployParamsMap =
        AppStaticGlobalDeployParametersEditComponent.convertObjectToStringMap(this.appDeploymentSpec.globalDeployParameters);
  }

  public getDeploymentParamsKeys(): string[] {
    return Array.from(this.globalDeployParamsMap.keys());
  }

}
