import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AppAccessMethod} from "../../../model/app-access-method";
import {ServiceAccessMethod, ServiceAccessMethodType} from "../../../model/serviceaccessmethod";

@Component({
  selector: 'app-access-method-edit',
  templateUrl: './app-access-method-edit.component.html',
  styleUrls: ['./app-access-method-edit.component.css']
})
export class AppAccessMethodEditComponent implements OnInit {

  public ServiceAccessMethodType = ServiceAccessMethodType;

  @Input()
  public id: number;

  @Input()
  public accessMethod: AppAccessMethod;

  @Input()
  public accessMethodTypes: string[];

  @Output()
  public output: EventEmitter<number> = new EventEmitter<number>();

  public deployParamsMap: Map<string, string> = new Map<string, string>();

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
    this.deployParamsMap = AppAccessMethodEditComponent.convertObjectToStringMap(this.accessMethod.deployParameters)
  }

  public isNewDeployParamValid(): boolean {
    if (!this.newKey || !this.newValue) {
      return false;
    }
    if (!!this.newKey && this.deployParamsMap.has(this.newKey)) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    if (this.isNewDeployParamValid()) {
      this.deployParamsMap.set(this.newKey, this.newValue);
      this.newKey = '';
      this.newValue = '';
    }
  }

  public removeDeployParam(key: string): void {
    if (this.deployParamsMap.has(key)) {
      this.deployParamsMap.delete(key);
    }
  }

  public updateDeployParams(): void {
    this.accessMethod.deployParameters = AppAccessMethodEditComponent.convertStringMapToObject(this.deployParamsMap);
  }

  public reject(): void {
    this.deployParamsMap = AppAccessMethodEditComponent.convertObjectToStringMap(this.accessMethod.deployParameters);
  }

  public getDeploymentParamsKeys(): string[] {
    return Array.from(this.deployParamsMap.keys());
  }

  public isDefault(): boolean {
    return ServiceAccessMethodType.DEFAULT === ServiceAccessMethod.getServiceAccessMethodTypeAsEnum(this.accessMethod.type);
  }

  public remove(): void {
    this.output.emit(this.id);
  }

}
