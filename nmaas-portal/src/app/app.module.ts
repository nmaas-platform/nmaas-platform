import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { routing }        from './app.routes';

import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { AppmarketComponent } from './appmarket/appmarket.component';
import { AppinstallComponent } from './appmarket/appinstall/appinstall.component';
import { AppdetailsComponent } from './appmarket/appdetails/appdetails.component';

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
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
