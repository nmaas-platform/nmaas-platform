import { RegistrationService } from '../auth/registration.service';
import { LoginComponent } from './login';
import { LogoutComponent } from './logout/logout.component';
import { PipesModule } from '../pipe/pipes.module';
import { SharedModule } from '../shared/shared.module';
import { RegistrationComponent } from './registration/registration.component';
import { WelcomeComponent } from './welcome.component';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthModule } from 'angular2-jwt';

@NgModule({
  declarations: [
    WelcomeComponent,
    LoginComponent,
    LogoutComponent,
    RegistrationComponent,
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    SharedModule,
    AuthModule,
    PipesModule,
  ],
  exports: [
    WelcomeComponent
  ],
  providers: [
    RegistrationService
  ]
})
export class WelcomeModule {}