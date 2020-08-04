import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppAbortModalComponent } from './app-abort-modal.component';

describe('AppAbortModalComponent', () => {
  let component: AppAbortModalComponent;
  let fixture: ComponentFixture<AppAbortModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppAbortModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppAbortModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
