import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppPreviewComponent } from './apppreview.component';
import {RouterTestingModule} from "@angular/router/testing";

describe('ApppreviewComponent', () => {
  let component: AppPreviewComponent;
  let fixture: ComponentFixture<AppPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppPreviewComponent ],
      imports: [RouterTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
