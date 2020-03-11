import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppAccessMethodEditComponent} from './app-access-method-edit.component';
import {FormsModule} from "@angular/forms";
import {TooltipModule} from "ng2-tooltip-directive";
import {ServiceAccessMethodType} from "../../../model/serviceaccessmethod";

describe('AppAccessMethodEditComponent', () => {
  let component: AppAccessMethodEditComponent;
  let fixture: ComponentFixture<AppAccessMethodEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppAccessMethodEditComponent ],
      imports: [FormsModule, TooltipModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppAccessMethodEditComponent);
    component = fixture.componentInstance;
    component.id = 0;
    component.accessMethodTypes = ['INTERNAL', 'EXTERNAL'];
    component.accessMethod = {
      type: ServiceAccessMethodType.INTERNAL,
      name: "d",
      tag: "t",
      deployParameters: {}
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
