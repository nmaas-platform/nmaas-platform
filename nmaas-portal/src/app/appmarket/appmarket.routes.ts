import { Route } from '@angular/router';
import { AppMarketComponent } from './appmarket.component';
import { AppListComponent } from './applist/index';
import { AppDetailsComponent } from './appdetails/index';

import { AuthGuard } from '../auth/auth.guard';

import { AppListRoutes } from './applist/applist.routes';
import { AppInstanceRoutes } from './appinstance/appinstance.routes';

export const AppMarketRoutes: Route[] = [
    {
      path: '',
      component: AppMarketComponent,
      canActivate: [AuthGuard],
      canActivateChild: [AuthGuard],
      children: [
        ...AppListRoutes,
        ...AppInstanceRoutes,
          { path: 'apps/:id', component: AppDetailsComponent },

      ]
    }
];
