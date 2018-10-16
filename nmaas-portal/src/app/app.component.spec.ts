/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule} from "@angular/router/testing";
import {AppConfigService, ConfigurationService} from "./service";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {TranslateService, TranslateModule, TranslateLoader} from "@ngx-translate/core";
import {TranslateFakeLoader} from "@ngx-translate/core";

describe('App: NmaasPortal', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
        imports: [
            RouterTestingModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            })
        ],
        providers: [
            AppConfigService,
            HttpClient,
            HttpHandler,
            ConfigurationService,
            TranslateService
        ]
    });
  });

  it('should create the app', async(() => {
    let fixture = TestBed.createComponent(AppComponent);
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
});
