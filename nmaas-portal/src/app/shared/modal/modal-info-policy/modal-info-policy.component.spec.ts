import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalInfoPolicyComponent } from './modal-info-policy.component';

describe('ModalInfoPolicyComponent', () => {
  let component: ModalInfoPolicyComponent;
  let fixture: ComponentFixture<ModalInfoPolicyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModalInfoPolicyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalInfoPolicyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
