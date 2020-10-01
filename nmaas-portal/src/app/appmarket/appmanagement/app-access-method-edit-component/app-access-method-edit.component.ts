import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AppAccessMethod} from '../../../model/app-access-method';
import {parseServiceAccessMethodType, ServiceAccessMethodType} from '../../../model/service-access-method';

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

  public newKey = '';
  public newValue = '';

  public defaultTooltipOptions = {
    'placement': 'bottom',
    'show-delay': '50',
    'theme': 'dark'
  };

  constructor() { }

  ngOnInit() {}

  public isNewDeployParamValid(): boolean {
    if (!this.newKey || !this.newValue) {
      return false;
    }
    if (!!this.newKey && this.accessMethod.deployParameters.hasOwnProperty(this.newKey) ) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    if (this.isNewDeployParamValid()) {
      this.accessMethod.deployParameters[this.newKey] = this.newValue
      this.newKey = '';
      this.newValue = '';
    }
  }

  public removeDeployParam(key: string): void {
    if (this.accessMethod.deployParameters.hasOwnProperty(key)) {
      delete this.accessMethod.deployParameters[key];
    }
  }

  public getDeploymentParamsKeys(): string[] {
    return Object.keys(this.accessMethod.deployParameters)
  }

  public isDefault(): boolean {
    return ServiceAccessMethodType.DEFAULT === parseServiceAccessMethodType(this.accessMethod.type);
  }

  public remove(): void {
    this.output.emit(this.id);
  }

}
