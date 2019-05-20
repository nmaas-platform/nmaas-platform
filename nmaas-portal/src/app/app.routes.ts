import { Routes, RouterModule } from '@angular/router';

import { AppMarketRoutes } from './appmarket';

import { AuthGuard } from './auth/auth.guard';
import { WelcomeRoutes } from './welcome/welcome.routes';
import {ServiceUnavailableRoutes} from "./service-unavailable/service-unavailable.routes";

const appRoutes: Routes = [
    ...WelcomeRoutes,
    ...AppMarketRoutes,
    ...ServiceUnavailableRoutes,
    { path: '**', redirectTo: '/welcome' }
];

export const routing = RouterModule.forRoot(appRoutes, {scrollPositionRestoration: 'enabled'});