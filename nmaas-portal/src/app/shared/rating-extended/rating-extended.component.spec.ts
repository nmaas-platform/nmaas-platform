import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RatingExtendedComponent } from './rating-extended.component';

describe('RatingExtendedComponent', () => {
  let component: RatingExtendedComponent;
  let fixture: ComponentFixture<RatingExtendedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RatingExtendedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RatingExtendedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
