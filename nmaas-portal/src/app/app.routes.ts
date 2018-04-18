import { Routes, RouterModule } from '@angular/router';

import { AppMarketRoutes } from './appmarket/index';

import { AuthGuard } from './auth/auth.guard';
import { WelcomeRoutes } from './welcome/welcome.routes';

const appRoutes: Routes = [
    ...WelcomeRoutes,
    ...AppMarketRoutes,
    { path: '**', redirectTo: '/welcome' }
];

export const routing = RouterModule.forRoot(appRoutes);