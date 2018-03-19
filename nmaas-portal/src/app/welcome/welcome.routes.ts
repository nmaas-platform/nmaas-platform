import { LoginComponent } from './login/login.component';
import { LogoutComponent } from './logout/logout.component';
import { RegistrationComponent } from './registration/registration.component';
import { Routes } from '@angular/router';
import { WelcomeComponent } from './welcome.component';

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
];
