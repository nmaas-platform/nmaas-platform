import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NmaasModalInfoTermsComponent } from './nmaas-modal-info-terms.component';

describe('NmaasModalInfoTermsComponent', () => {
  let component: NmaasModalInfoTermsComponent;
  let fixture: ComponentFixture<NmaasModalInfoTermsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NmaasModalInfoTermsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NmaasModalInfoTermsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
