import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { JwtModule } from '@auth0/angular-jwt';

import { routing } from './app.routes';

import { AppComponent } from './app.component';

import { AppConfigService } from './service/appconfig.service';

import { WelcomeModule } from './welcome/welcome.module';
import { AppMarketModule } from './appmarket/index';
import { SharedModule } from './shared/index';

import { AuthGuard } from './auth/auth.guard';
import { AuthService } from './auth/auth.service';
import { LoginComponent } from './welcome/login/login.component';
import { LogoutComponent } from './welcome/logout/logout.component';

import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { CORSHeaderInterceptor } from "./interceptor/corsheader.interceptor";

export function appConfigFactory( config: AppConfigService) {
  return function create() {
    return config.load();
  }
}

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    JwtModule.forRoot({
        config: {
          tokenGetter: () => {
            return localStorage.getItem('token');
          },
        whitelistedDomains: [new RegExp("[\s\S]")]
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
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CORSHeaderInterceptor,
      multi: true

    }
  ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
