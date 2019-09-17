import {LoginComponent} from './login/login.component';
import {LogoutComponent} from './logout/logout.component';
import {RegistrationComponent} from './registration/registration.component';
import {Routes} from '@angular/router';
import {WelcomeComponent} from './welcome.component';
import {ProfileComponent} from "./profile/profile.component";
import {AuthGuard} from "../auth/auth.guard";
import {ComponentMode} from "../shared";
import {CompleteComponent} from "./complete/complete.component";
import {TermsAcceptanceComponent} from "./terms-acceptance/terms-acceptance.component";
import {PasswordResetComponent} from "./passwordreset/password-reset.component";
import {PrivacyPolicySubpageComponent} from "./privacy-policy-subpage/privacy-policy-subpage.component";
import {AboutComponent} from "../shared/about/about.component";

export const WelcomeRoutes: Routes = [
    {
      path: 'welcome',
      component: WelcomeComponent,
      children: [
        { path: '', redirectTo: 'login', pathMatch: 'full'  },
        { path: 'login', component: LoginComponent},
        { path: 'registration', component: RegistrationComponent }
      ]
    },
    { path: 'logout', component: LogoutComponent },
    { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard], data: {mode: ComponentMode.VIEW} },
    { path: 'complete', component: CompleteComponent, canActivate: [AuthGuard] },
    { path: 'terms-acceptance', component: TermsAcceptanceComponent, canActivate: [AuthGuard]},
    { path: 'reset/:token', component: PasswordResetComponent },
    { path: 'privacy', component: PrivacyPolicySubpageComponent},
    { path: 'about', component: AboutComponent}
];
