import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {JWT_OPTIONS, JwtModule} from '@auth0/angular-jwt';
import {AppConfigService} from '../service/appconfig.service';
import {AuthService} from './auth.service'
import {AuthGuard} from './auth.guard'
import {RoleGuard} from './role.guard';


export const jwtOptionsFactory = (appConfig: AppConfigService) => ({
    tokenGetter: () => {
        return localStorage.getItem(appConfig.config.tokenName ? appConfig.config.tokenName : 'token');
    },
    whitelistedDomains: [new RegExp("[\s\S]*")]
});

@NgModule({
  providers: [
    AuthGuard,
    RoleGuard,
    AuthService
  ],
  imports: [
      HttpClientModule,
      JwtModule.forRoot({
          jwtOptionsProvider: {
              provide: JWT_OPTIONS,
              deps: [AppConfigService],
              useFactory: jwtOptionsFactory
          }
      })
  ]
})
export class AuthModule {}
