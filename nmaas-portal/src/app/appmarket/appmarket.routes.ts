import { Route } from '@angular/router';
import { AppMarketComponent } from './index';
import { AppListComponent } from './applist/index';
import { AppDetailsComponent } from './appdetails/index';
import { AppInstallComponent } from './appinstall/index';

import { AuthGuard } from '../auth/auth.guard';

import { AppListRoutes } from './applist/applist.routes';

export const AppMarketRoutes: Route[] = [
    {
      path: '',
      component: AppMarketComponent,
      canActivate: [AuthGuard],
      canActivateChild: [AuthGuard],
      children: [
        ...AppListRoutes,
          { path: 'apps/:id', component: AppDetailsComponent },
          { path: 'apps/:id/install', component: AppInstallComponent }
      ]
    }
];