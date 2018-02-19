import { Routes, RouterModule } from '@angular/router';

import { LoginRoutes } from './login/index';
import { AppMarketRoutes } from './appmarket/index';

import { AuthGuard } from './auth/auth.guard';

const appRoutes: Routes = [
    ...LoginRoutes,
    ...AppMarketRoutes,
    { path: '**', redirectTo: '' }
];

export const routing = RouterModule.forRoot(appRoutes);