import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ServiceAccessMethod, ServiceAccessMethodType} from '../../../../model/serviceaccessmethod';
import {ModalComponent} from '../../../../shared/modal';
import {isNullOrUndefined} from 'util';

@Component({
  selector: 'app-access-methods-modal',
  templateUrl: './access-methods-modal.component.html',
  styleUrls: ['./access-methods-modal.component.css']
})
export class AccessMethodsModalComponent implements OnInit {

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  @Input()
  public accessMethods: ServiceAccessMethod[];

  public defaultAccessMethod: ServiceAccessMethod = undefined;
  public externalAccessMethods: ServiceAccessMethod[] = [];
  public internalAccessMethods: ServiceAccessMethod[] = [];

  constructor() { }

  ngOnInit() {
    if (this.accessMethods) {
      this.defaultAccessMethod = this.accessMethods.find(s => this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.DEFAULT);
      this.externalAccessMethods = this.accessMethods.filter(s => this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.EXTERNAL);
      this.internalAccessMethods = this.accessMethods.filter(s => this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.INTERNAL);
    }
  }

  public accessMethodTypeAsEnum(a: ServiceAccessMethodType | string): ServiceAccessMethodType {
    if (typeof  a === 'string') {
      return ServiceAccessMethodType[a];
    }
    return a;
  }

  public validateURL(url: string): string {
    if (isNullOrUndefined(url)) {
      return '';
    }
    if (url.startsWith('http://')) {
      return url.replace('http://', 'https://');
    }
    if (url.startsWith('https://')) {
      return url
    }
    return 'https://' + url;
  }

  public show() {
    this.modal.show();
  }

  public hide() {
    this.modal.hide();
  }

}
