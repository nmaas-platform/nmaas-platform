import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AppStorageVolume} from '../../../model/app-storage-volume';
import {ServiceStorageVolume, ServiceStorageVolumeType} from '../../../model/servicestoragevolume';

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
    if (!!this.newKey && this.storageVolume.deployParameters.hasOwnProperty(this.newKey)) {
      return false;
    }
    return (!!this.newKey && !!this.newValue);
  }

  public addNewDeployParam(): void {
    if (this.isNewDeployParamValid()) {
      this.storageVolume.deployParameters[this.newKey] = this.newValue;
      this.newKey = '';
      this.newValue = '';
    }
  }

  public removeDeployParam(key: string): void {
    if (this.storageVolume.deployParameters.hasOwnProperty(key)) {
      delete this.storageVolume.deployParameters[key];
    }
  }

  public getDeploymentParamsKeys(): string[] {
    return Object.keys(this.storageVolume.deployParameters)
  }

  public isMain(): boolean {
    return ServiceStorageVolumeType.MAIN === ServiceStorageVolume.getServiceStorageVolumeTypeAsEnum(this.storageVolume.type);
  }

  public remove(): void {
    this.output.emit(this.id);
  }

}
