import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SshShellComponent } from './ssh-shell.component';
import {NgTerminalModule} from 'ng-terminal';


describe('SshShellComponent', () => {
  let component: SshShellComponent;
  let fixture: ComponentFixture<SshShellComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
          SshShellComponent,
      ],
      imports: [
          NgTerminalModule
      ]
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
