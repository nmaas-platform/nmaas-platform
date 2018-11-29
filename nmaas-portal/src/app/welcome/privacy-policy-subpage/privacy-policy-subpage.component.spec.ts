import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PrivacyPolicySubpageComponent } from './privacy-policy-subpage.component';

describe('PrivacyPolicySubpageComponent', () => {
  let component: PrivacyPolicySubpageComponent;
  let fixture: ComponentFixture<PrivacyPolicySubpageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PrivacyPolicySubpageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PrivacyPolicySubpageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
