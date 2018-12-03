/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AppInstallModalComponent } from './appinstallmodal.component';

describe('AppInstallmodalComponent', () => {
  let component: AppInstallModalComponent;
  let fixture: ComponentFixture<AppInstallModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppInstallModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppInstallModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
