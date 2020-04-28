import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AppStorageVolume} from "../../../model/app-storage-volume";
import {ServiceStorageVolume, ServiceStorageVolumeType} from "../../../model/servicestoragevolume";

@Component({
  selector: 'app-storage-volume-edit',
  templateUrl: './app-storage-volume-edit.component.html',
  styleUrls: ['./app-storage-volume-edit.component.css']
})
export class AppStorageVolumeEditComponent implements OnInit {

  public ServiceStorageVolumeType = ServiceStorageVolumeType;

  @Input()
  public id: number;

  @Input()
  public storageVolume: AppStorageVolume;

  @Input()
  public storageVolumeTypes: string[];

  @Output()
  public output: EventEmitter<number> = new EventEmitter<number>();

  public deployParamsMap: Map<string, string> = new Map<string, string>();

  public newKey = '';
  public newValue = '';

  public defaultTooltipOptions = {
    'placement': 'bottom',
    'show-delay': "50",
    'theme': 'dark'
  };

  constructor() { }

  ngOnInit() {
    this.deployParamsMap = AppStorageVolumeEditComponent.convertObjectToStringMap(this.storageVolume.deployParameters)
  }

  private static convertObjectToStringMap(arg: object): Map<string, string> {
    let result = new Map<string, string>();
    for(let k of Object.keys(arg)) {
      if(!result.has(k) && typeof arg[k] === 'string') {
        result.set(k, arg[k]);
      }
    }
    return result;
  }

  private static convertStringMapToObject(arg: Map<string, string>): object {
    const result = {};
    for(let k of arg.keys()) {
      result[k] = arg.get(k);
    }
    return result;
  }

  public isNewDeployParamValid(): boolean {
    if (!this.newKey || !this.newValue) {
      return false;
    }
    if(!!this.newKey && this.deployParamsMap.has(this.newKey)) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    if (this.isNewDeployParamValid()){
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
    this.storageVolume.deployParameters = AppStorageVolumeEditComponent.convertStringMapToObject(this.deployParamsMap);
  }

  public reject(): void {
    this.deployParamsMap = AppStorageVolumeEditComponent.convertObjectToStringMap(this.storageVolume.deployParameters);
  }

  public getDeploymentParamsKeys(): string[] {
    return Array.from(this.deployParamsMap.keys());
  }

  public isMain(): boolean {
    return ServiceStorageVolumeType.MAIN === ServiceStorageVolume.getServiceStorageVolumeTypeAsEnum(this.storageVolume.type);
  }

  public remove(): void {
    this.output.emit(this.id);
  }

}
