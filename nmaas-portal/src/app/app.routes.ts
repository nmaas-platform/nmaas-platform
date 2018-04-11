import { Routes, RouterModule } from '@angular/router';

import { AppMarketRoutes } from './appmarket/index';
import { ChangelogRoutes } from './changelog/index';

import { AuthGuard } from './auth/auth.guard';
import { WelcomeRoutes } from './welcome/welcome.routes';

const appRoutes: Routes = [
    ...WelcomeRoutes,
    ...AppMarketRoutes,
    ...ChangelogRoutes,
    { path: '**', redirectTo: '/welcome' }
];

export const routing = RouterModule.forRoot(appRoutes);