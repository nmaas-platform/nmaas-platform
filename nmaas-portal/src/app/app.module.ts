import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {JWT_OPTIONS, JwtModule} from '@auth0/angular-jwt';

import { routing } from './app.routes';

import { AppComponent } from './app.component';

import { AppConfigService } from './service/appconfig.service';

import { WelcomeModule } from './welcome/welcome.module';
import { AppMarketModule } from './appmarket/index';
import { SharedModule } from './shared/index';

import { AuthGuard } from './auth/auth.guard';
import { AuthService } from './auth/auth.service';

import {TranslateModule, TranslateLoader} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateService} from "@ngx-translate/core";
import {TranslateLoaderImpl} from "./service/translate-loader-impl.service";


export function appConfigFactory( config: AppConfigService) {
  return function create() {
    return config.load();
  }
}

export const jwtOptionsFactory = (appConfig: AppConfigService) => ({
    tokenGetter: () => {
        return localStorage.getItem('token'); //TODO: change this to be able to replace 'token' with definied name
    },
    whitelistedDomains: [new RegExp("[\s\S]*")]
});

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    JwtModule.forRoot({
        jwtOptionsProvider: {
            provide: JWT_OPTIONS,
            deps: [AppConfigService],
            useFactory: jwtOptionsFactory
        }
    }),
    AppMarketModule,
    SharedModule,
    WelcomeModule,
    routing,
    TranslateModule.forRoot({
      loader: {
          provide: TranslateLoader,
          useFactory: HttpLoaderFactory,
          deps: [HttpClient, AppConfigService]
      }
    })
  ],
  providers: [
    AuthGuard,
    AuthService,
    AppConfigService,
    {
        provide: APP_INITIALIZER,
        useFactory: appConfigFactory,
        deps: [ AppConfigService ],
        multi: true,
    },
      TranslateService
  ],
    exports:[
      TranslateModule
    ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }

export function HttpLoaderFactory(httpClient: HttpClient, appConfig: AppConfigService) {
    // return new TranslateHttpLoader(httpClient);// Use this if you want to get the language json from local asset folder
  return new TranslateLoaderImpl(httpClient, appConfig);
}

