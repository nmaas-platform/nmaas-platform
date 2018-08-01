import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ConfigurationDetailsComponent} from './configurationdetails.component';

describe('ConfigurationDetailsComponent', () => {
  let component: ConfigurationDetailsComponent;
  let fixture: ComponentFixture<ConfigurationDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigurationDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigurationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
