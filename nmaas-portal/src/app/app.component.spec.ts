/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule} from '@angular/router/testing';
import {AppConfigService, ConfigurationService} from './service';
import {HttpClient, HttpHandler} from '@angular/common/http';
import {TranslateService, TranslateModule, TranslateLoader, MissingTranslationHandler} from '@ngx-translate/core';
import {TranslateFakeLoader} from '@ngx-translate/core';
import {Observable, of} from 'rxjs';
import {Configuration} from './model/configuration';
import {CustomMissingTranslationService} from './i18n/custommissingtranslation.service';
import {AuthService} from './auth/auth.service';
import {JwtHelperService, JwtModule} from '@auth0/angular-jwt';
import {ServiceUnavailableService} from './service-unavailable/service-unavailable.service';
import {SharedModule} from './shared';

class MockConfigurationService {
  protected uri: string;

  constructor() {
    this.uri = 'http://localhost/api';
  }

  public getApiUrl(): string {
    return 'http://localhost/api';
  }

  public getConfiguration(): Observable<Configuration> {
    return of<Configuration>();
  }

  public updateConfiguration(configuration: Configuration): Observable<any> {
    return of<Configuration>();
  }
}

class MockServiceUnavailableService {
  public isServiceAvailable: boolean;

  constructor() {
    this.isServiceAvailable = true;
  }
}

describe('App: NmaasPortal', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
        imports: [
            RouterTestingModule,
            TranslateModule.forRoot({
                missingTranslationHandler: {provide: MissingTranslationHandler, useClass: CustomMissingTranslationService},
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            }),
            JwtModule.forRoot({
                config: {
                    tokenGetter: () => {
                        return '';
                    }
                }
            }),
          SharedModule
        ],
        providers: [
            {provide: AppConfigService, useClass: MockConfigurationService},
            HttpClient,
            HttpHandler,
            ConfigurationService,
            TranslateService,
            AuthService,
            JwtHelperService,
            {provide: ServiceUnavailableService, useClass: MockServiceUnavailableService}
        ]
    });
  });

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
});
