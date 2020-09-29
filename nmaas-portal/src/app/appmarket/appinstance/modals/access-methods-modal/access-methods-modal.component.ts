import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ServiceAccessMethod, ServiceAccessMethodType} from '../../../../model/service-access-method';
import {ModalComponent} from '../../../../shared/modal';

@Component({
  selector: 'app-access-methods-modal',
  templateUrl: './access-methods-modal.component.html',
  styleUrls: ['./access-methods-modal.component.css']
})
export class AccessMethodsModalComponent implements OnInit {

  @ViewChild(ModalComponent, { static: true })
  public readonly modal: ModalComponent;

  @Input()
  public accessMethods: ServiceAccessMethod[];

  public externalAccessMethods: ServiceAccessMethod[] = [];
  public internalAccessMethods: ServiceAccessMethod[] = [];
  public publicAccessMethods: ServiceAccessMethod[] = [];

  constructor() { }

  ngOnInit() {
    if (this.accessMethods) {
      this.externalAccessMethods = this.accessMethods.filter(s => this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.EXTERNAL
          || this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.DEFAULT);
      this.internalAccessMethods = this.accessMethods.filter(s => this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.INTERNAL);
      this.publicAccessMethods = this.accessMethods.filter(s => this.accessMethodTypeAsEnum(s.type) === ServiceAccessMethodType.PUBLIC);
    }
  }

  public accessMethodTypeAsEnum(a: ServiceAccessMethodType | string): ServiceAccessMethodType {
    if (typeof  a === 'string') {
      return ServiceAccessMethodType[a];
    }
    return a;
  }

  public validateURL(url: string): string {
    if (url == null) {
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
