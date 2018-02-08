import {NgModule} from '@angular/core';
import {Http, RequestOptions} from '@angular/http';
import {AuthHttp, AuthConfig} from 'angular2-jwt';
import {AppConfigService} from '../service/appconfig.service';
import {AuthService} from './auth.service'
import {AuthGuard} from './auth.guard'
import {RoleGuard} from './role.guard';

export function authHttpServiceFactory(http: Http, options: RequestOptions, appConfig: AppConfigService) {
  return new AuthHttp(new AuthConfig({
    tokenName: (appConfig.config.tokenName ? appConfig.config.tokenName : 'token'),
    tokenGetter: (() => localStorage.getItem(this.tokenName)),
    globalHeaders: [{'Content-Type': 'application/json', 'Accept': 'application/json'}],
  }), http, options);
}

@NgModule({
  providers: [
    AuthGuard,
    RoleGuard,
    AuthService,
    {
      provide: AuthHttp,
      useFactory: authHttpServiceFactory,
      deps: [Http, RequestOptions, AppConfigService]
    }
  ]
})
export class AuthModule {}
