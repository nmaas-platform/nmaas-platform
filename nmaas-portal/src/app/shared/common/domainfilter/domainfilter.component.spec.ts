import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainFilterComponent } from './domainfilter.component';

describe('DomainFilterComponent', () => {
  let component: DomainFilterComponent;
  let fixture: ComponentFixture<DomainFilterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainFilterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  //it('should create', () => {
  //  expect(component).toBeTruthy();
  //});
});
