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
import {TranslateModule, TranslateLoader} from '@ngx-translate/core';
import {HttpClient} from '@angular/common/http';
import {HttpLoaderFactory} from '../app.module';
import {ShibbolethService} from "../service/shibboleth.service";

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