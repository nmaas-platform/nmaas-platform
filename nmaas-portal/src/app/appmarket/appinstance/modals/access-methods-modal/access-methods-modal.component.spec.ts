import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AccessMethodsModalComponent} from './access-methods-modal.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../../shared';
import {ServiceAccessMethod, ServiceAccessMethodType} from '../../../../model/service-access-method';

describe('AccessMethodsModalComponent', () => {
  let component: AccessMethodsModalComponent;
  let fixture: ComponentFixture<AccessMethodsModalComponent>;

  const am: ServiceAccessMethod[] = [
    {type: ServiceAccessMethodType.DEFAULT, name: 'Default', protocol: 'Web', url: 'https://some.default.url'},
    {type: ServiceAccessMethodType.EXTERNAL, name: 'External-1', protocol: 'Web', url: 'external.org'},
    {type: ServiceAccessMethodType.EXTERNAL, name: 'External-2', protocol: 'Web', url: 'http://external.stack.org'},
    {type: ServiceAccessMethodType.INTERNAL, name: 'SSH access', protocol: 'SSH', url: 'ssh user@remote'},
    {type: ServiceAccessMethodType.LOCAL, name: 'Local', protocol: 'in-cluster', url: 'http://local-service-name'},
  ];

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessMethodsModalComponent ],
      imports: [
          SharedModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        }),
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessMethodsModalComponent);
    component = fixture.componentInstance;
    component.accessMethods = am;
    component.modal.show();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have inner list', () => {
    expect(component.accessMethods.length).toEqual(am.length);
    expect(component.externalAccessMethods.length).toEqual(3);
    expect(component.internalAccessMethods.length).toEqual(1);
    expect(component.localAccessMethods.length).toEqual(1);
  });
});
