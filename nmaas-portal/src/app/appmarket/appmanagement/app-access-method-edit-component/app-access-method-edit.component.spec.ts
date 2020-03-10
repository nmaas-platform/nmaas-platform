import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppAccessMethodEditComponent } from './app-access-method-edit.component';

describe('AppAccessMethodEditComponent', () => {
  let component: AppAccessMethodEditComponent;
  let fixture: ComponentFixture<AppAccessMethodEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppAccessMethodEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppAccessMethodEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
