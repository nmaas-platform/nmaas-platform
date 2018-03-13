import { Routes, RouterModule } from '@angular/router';

import { LoginRoutes } from './login/index';
import { AppMarketRoutes } from './appmarket/index';
import { ChangelogRoutes } from './changelog/index';

import { AuthGuard } from './auth/auth.guard';

const appRoutes: Routes = [
    ...LoginRoutes,
    ...AppMarketRoutes,
    ...ChangelogRoutes,
    { path: '**', redirectTo: '' }
];

export const routing = RouterModule.forRoot(appRoutes);
