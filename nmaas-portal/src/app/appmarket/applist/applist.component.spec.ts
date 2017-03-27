/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AppListComponent } from './applist.component';

describe('ApplistComponent', () => {
  let component: ApplistComponent;
  let fixture: ComponentFixture<ApplistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
