import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseComponent } from './base.component';
import {ComponentMode} from "../componentmode";

describe('BaseComponent', () => {
  let component: BaseComponent;
  let fixture: ComponentFixture<BaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return that VIEW is allowed', ()=>{
    expect(component.isCurrentModeAllowed()).toBe(true);
  });

  it('should return VIEW mode', ()=>{
    expect(component.getCurrentMode()).toBe(ComponentMode.VIEW);
  });

  it('should get mode VIEW', ()=>{
    expect(component.getMode(undefined)).toBe(ComponentMode.VIEW);
  });

});
