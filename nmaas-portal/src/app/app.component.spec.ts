/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule} from "@angular/router/testing";
import {AppConfigService, ConfigurationService} from "./service";
import {HttpClient, HttpHandler} from "@angular/common/http";

describe('App: NmaasPortal', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
        imports: [
            RouterTestingModule
        ],
        providers: [
            AppConfigService,
            HttpClient,
            HttpHandler,
            ConfigurationService
        ]
    });
  });

  it('should create the app', async(() => {
    let fixture = TestBed.createComponent(AppComponent);
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
});
