import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalProvideSshKeyComponent } from './modal-provide-ssh-key.component';

describe('ModalProvideSshKeyComponent', () => {
  let component: ModalProvideSshKeyComponent;
  let fixture: ComponentFixture<ModalProvideSshKeyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModalProvideSshKeyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalProvideSshKeyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // it('should create', () => {
  //   expect(component).toBeTruthy();
  // });
});
