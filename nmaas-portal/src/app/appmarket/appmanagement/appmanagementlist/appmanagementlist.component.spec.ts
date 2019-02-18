import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppManagementListComponent } from './appmanagementlist.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {AppConfigService, AppsService} from "../../../service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {AuthService} from "../../../auth/auth.service";
import {MockAuthService} from "../../navbar/navbar.component.spec";

describe('AppManagementListComponent', () => {
  let component: AppManagementListComponent;
  let fixture: ComponentFixture<AppManagementListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppManagementListComponent ],
      providers:[AppsService, AppConfigService, {provide: AuthService, useClass: MockAuthService}],
      imports:[
        HttpClientTestingModule,
        RouterTestingModule,
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
