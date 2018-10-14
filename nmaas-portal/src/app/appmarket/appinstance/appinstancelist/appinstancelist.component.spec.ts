/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AppInstanceListComponent } from './appinstancelist.component';

describe('AppInstanceListComponent', () => {
  let component: AppInstanceListComponent;
  let fixture: ComponentFixture<AppInstanceListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppInstanceListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppInstanceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
  
});
