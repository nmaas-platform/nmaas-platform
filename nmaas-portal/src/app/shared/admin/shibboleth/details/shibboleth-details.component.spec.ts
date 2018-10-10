import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ShibbolethDetailsComponent } from './shibboleth-details.component';

describe('ShibbolethDetailsComponent', () => {
  let component: ShibbolethDetailsComponent;
  let fixture: ComponentFixture<ShibbolethDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShibbolethDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShibbolethDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  //it('should create', () => {
  //  expect(component).toBeTruthy();
  //});
});
