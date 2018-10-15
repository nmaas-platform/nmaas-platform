/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AppListComponent } from './applist.component';
import {RouterTestingModule} from "@angular/router/testing";
import {AppSubscriptionsService} from "../../service/appsubscriptions.service";
import {UserDataService} from "../../service/userdata.service";
import {ApplicationsViewComponent} from "../../shared/applications/applications.component";
import {AppConfigService, AppsService} from "../../service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ApplistComponent', () => {
  let component: AppListComponent;
  let fixture: ComponentFixture<AppListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppListComponent ],
        imports:[
            RouterTestingModule,
            HttpClientTestingModule,

        ],
        providers: [
            AppConfigService,
            ApplicationsViewComponent,
            AppSubscriptionsService,
            UserDataService,
            AppsService,

        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
