import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HttpModule, Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';
import { AuthHttp, AuthConfig } from 'angular2-jwt';

import { routing }        from './app.routes';

import { AppComponent } from './app.component';

import { AppConfigService } from './service/appconfig.service';

import { LoginComponent } from './login/login.component';
import { AppMarketModule } from './appmarket/index';
import { SharedModule } from './shared/index';

import { AuthGuard } from './auth/auth.guard';
import { AuthService } from './auth/auth.service';
import { LogoutComponent } from './logout/logout.component';


export function appConfigFactory( config: AppConfigService) {
	return function create() {
		return config.load();
	}
}

export function authHttpServiceFactory(http: Http, options: RequestOptions) {
  return new AuthHttp(new AuthConfig({
        tokenName: 'token',
        tokenGetter: (() => localStorage.getItem('token')),
        globalHeaders: [{'Content-Type':'application/json', 'Accept': 'application/json'}],
    }), http, options);
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LogoutComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppMarketModule,
    SharedModule,
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
      provide: AuthHttp,
      useFactory: authHttpServiceFactory,
      deps: [Http, RequestOptions]
    }
  ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
