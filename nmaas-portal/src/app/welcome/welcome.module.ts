import {RegistrationService} from '../auth/registration.service';
import {LoginComponent} from './login';
import {LogoutComponent} from './logout/logout.component';
import {ChangelogComponent} from './changelog/changelog.component';
import {PipesModule} from '../pipe/pipes.module';
import {SharedModule} from '../shared/shared.module';
import {RegistrationComponent} from './registration/registration.component';
import {ChangelogService} from '../service/changelog.service';
import {WelcomeComponent} from './welcome.component';
import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {AppMarketModule} from '../appmarket';
import {UserService} from '../service';
import {CompleteComponent} from './complete/complete.component';
import {ContentDisplayService} from '../service/content-display.service';
import {TermsAcceptanceComponent} from './terms-acceptance/terms-acceptance.component';
import {ReCaptchaModule} from 'angular5-recaptcha';
import {TranslateModule} from '@ngx-translate/core';
import {ShibbolethService} from '../service/shibboleth.service';
import { PasswordResetComponent } from './passwordreset/password-reset.component';
import { PrivacyPolicySubpageComponent } from './privacy-policy-subpage/privacy-policy-subpage.component';
import {PasswordStrengthMeterModule} from "angular-password-strength-meter";

@NgModule({
  declarations: [
    WelcomeComponent,
    LoginComponent,
    LogoutComponent,
    ChangelogComponent,
    RegistrationComponent,
    ProfileComponent,
    CompleteComponent,
    TermsAcceptanceComponent,
    PasswordResetComponent,
    PrivacyPolicySubpageComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    SharedModule,
    PipesModule,
    AppMarketModule,
    ReCaptchaModule,
    PasswordStrengthMeterModule,
    TranslateModule.forChild()
  ],
  exports: [
    WelcomeComponent
  ],
  providers: [
    RegistrationService,
    UserService,
    ChangelogService,
    ContentDisplayService,
    ShibbolethService
  ]
})
export class WelcomeModule {}