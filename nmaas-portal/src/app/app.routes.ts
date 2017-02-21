import { Routes, RouterModule } from '@angular/router';

import { LoginComponent } from './login/index';
import { AppmarketComponent } from './appmarket/index';
import { AppinstallComponent } from './appmarket/appinstall/index';
import { AppdetailsComponent } from './appmarket/appdetails/index';

import { AuthGuard } from './auth/auth.guard';

const appRoutes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: '', component: AppmarketComponent, canActivate: [AuthGuard] },

    { path: 'app/install', component: AppinstallComponent },
    { path: 'app/details', component: AppdetailsComponent }, 
    // otherwise redirect to home
    { path: '**', redirectTo: '' }
];

export const routing = RouterModule.forRoot(appRoutes);