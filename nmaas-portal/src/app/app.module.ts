import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { routing }        from './app.routes';

import { AppComponent } from './app.component';

import { AppConfigService } from './service/appconfig.service';

import { LoginComponent } from './login/login.component';
import { AppmarketComponent } from './appmarket/appmarket.component';
import { AppinstallComponent } from './appmarket/appinstall/appinstall.component';
import { AppdetailsComponent } from './appmarket/appdetails/appdetails.component';

import { AuthGuard } from './auth/auth.guard';
import { AuthService } from './auth/auth.service';

export function appConfigFactory( config: AppConfigService) {
	return function create() {
		return config.load();
	}
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    AppmarketComponent,
    AppinstallComponent,
    AppdetailsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    routing
  ],
  providers: [
    AuthGuard,
    AuthService,
    AppConfigService,
    {   provide: APP_INITIALIZER,
        useFactory: appConfigFactory,
        deps: [ AppConfigService ],
        multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
