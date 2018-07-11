import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppRestartModalComponent } from './app-restart-modal.component';

describe('AppRestartModalComponent', () => {
  let component: AppRestartModalComponent;
  let fixture: ComponentFixture<AppRestartModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppRestartModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppRestartModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
