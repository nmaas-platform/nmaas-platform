import { RegistrationService } from '../auth/registration.service';
import { LoginComponent } from './login';
import { LogoutComponent } from './logout/logout.component';
import { ChangelogComponent } from './changelog/changelog.component';
import { PipesModule } from '../pipe/pipes.module';
import { SharedModule } from '../shared/shared.module';
import { RegistrationComponent } from './registration/registration.component';
import { ChangelogService } from '../service/changelog.service';
import { WelcomeComponent } from './welcome.component';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from "@angular/common/http";
import { JwtModule } from '@auth0/angular-jwt';

@NgModule({
  declarations: [
    WelcomeComponent,
    LoginComponent,
    LogoutComponent,
    ChangelogComponent,
    RegistrationComponent,
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    SharedModule,
    HttpClientModule,
    JwtModule,
    PipesModule,
  ],
  exports: [
    WelcomeComponent
  ],
  providers: [
    RegistrationService,
    ChangelogService
  ]
})
export class WelcomeModule {}