import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppVersionCreateWizardComponent } from './appversioncreatewizard.component';

describe('AppversioncreatewizardComponent', () => {
  let component: AppVersionCreateWizardComponent;
  let fixture: ComponentFixture<AppVersionCreateWizardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppVersionCreateWizardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppVersionCreateWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
