import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppManagementListComponent } from './appmanagementlist.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {AppConfigService, AppsService} from "../../../service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('AppManagementListComponent', () => {
  let component: AppManagementListComponent;
  let fixture: ComponentFixture<AppManagementListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppManagementListComponent ],
      providers:[AppsService, AppConfigService],
      imports:[
        HttpClientTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        })
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppManagementListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
