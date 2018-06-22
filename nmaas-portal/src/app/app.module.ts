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

import { HttpClientModule } from '@angular/common/http';

export function appConfigFactory( config: AppConfigService) {
  return function create() {
    return config.load();
  }
}

export const jwtOptionsFactory = (appConfig: AppConfigService) => ({
    tokenGetter: () => {
        return localStorage.getItem('token'); //TODO: change this to be able to replace 'token' with definied name
    },
    whitelistedDomains: [new RegExp("[\s\S]")]
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
    routing
  ],
  providers: [
    AuthGuard,
    AuthService,
    AppConfigService,
    {
        provide: APP_INITIALIZER,
        useFactory: appConfigFactory,
        deps: [ AppConfigService ],
        multi: true
    }
  ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
