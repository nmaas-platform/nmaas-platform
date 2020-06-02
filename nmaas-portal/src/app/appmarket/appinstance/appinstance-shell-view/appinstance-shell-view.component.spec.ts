import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppInstanceShellViewComponent } from './appinstance-shell-view.component';

describe('AppInstanceShellViewComponent', () => {
  let component: AppInstanceShellViewComponent;
  let fixture: ComponentFixture<AppInstanceShellViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppInstanceShellViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppInstanceShellViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
