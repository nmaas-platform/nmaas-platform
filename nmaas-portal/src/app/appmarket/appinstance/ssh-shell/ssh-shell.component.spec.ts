import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SshShellComponent } from './ssh-shell.component';

describe('SshShellComponent', () => {
  let component: SshShellComponent;
  let fixture: ComponentFixture<SshShellComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SshShellComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SshShellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
