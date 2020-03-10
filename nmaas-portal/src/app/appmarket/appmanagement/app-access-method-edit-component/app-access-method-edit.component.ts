import {Component, Input, OnInit} from '@angular/core';
import {AppAccessMethod} from "../../../model/app-access-method";

@Component({
  selector: 'app-access-method-edit',
  templateUrl: './app-access-method-edit.component.html',
  styleUrls: ['./app-access-method-edit.component.css']
})
export class AppAccessMethodEditComponent implements OnInit {

  @Input()
  public id: number;

  @Input()
  public accessMethod: AppAccessMethod;

  private deployParamsMap: Map<string, string>;

  constructor() { }

  ngOnInit() {
    this.deployParamsMap = AppAccessMethodEditComponent.convertToStringMap(this.accessMethod.deployParameters)
  }

  private static convertToStringMap(arg: Map<string, string> | object): Map<string, string> {
    if(arg instanceof Map) {
      return arg;
    }
    let result = new Map<string, string>();
    for(let k of Object.keys(arg)) {
      if(!result.has(k) && typeof arg[k] === 'string') {
        result.set(k, arg[k]);
      }
    }
    return result;
  }

  addNewDeployParam(): void {
    // TODO
  }

  removeDeployParam(key: string): void {
    // TODO
  }

  updateDeployParams(): void {
    // TRUE
    // update params in accessMethodObject
    this.accessMethod.deployParameters = this.deployParamsMap;
  }

}
