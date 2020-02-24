import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AccessMethodsModalComponent} from './access-methods-modal.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {SharedModule} from "../../../../shared";
import {ServiceAccessMethod, ServiceAccessMethodType} from "../../../../model/serviceaccessmethod";

describe('AccessMethodsModalComponent', () => {
  let component: AccessMethodsModalComponent;
  let fixture: ComponentFixture<AccessMethodsModalComponent>;

  let am: ServiceAccessMethod[] = [
    {type: ServiceAccessMethodType.DEFAULT, name: 'Default', url: 'https://some.default.url'},
    {type: ServiceAccessMethodType.EXTERNAL, name: 'External-1', url: 'external.org'},
    {type: ServiceAccessMethodType.EXTERNAL, name: 'External-2', url: 'http://external.stack.org'},
    {type: ServiceAccessMethodType.INTERNAL, name: 'SSH access', url: 'ssh user@remote'},
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
    expect(component.defaultAccessMethod).toBeDefined();
    expect(component.externalAccessMethods.length).toEqual(2);
    expect(component.internalAccessMethods.length).toEqual(1);
  });
});
