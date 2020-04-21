import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewSshKeyComponent } from './new-ssh-key.component';

describe('NewSshKeyComponent', () => {
  let component: NewSshKeyComponent;
  let fixture: ComponentFixture<NewSshKeyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewSshKeyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewSshKeyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
