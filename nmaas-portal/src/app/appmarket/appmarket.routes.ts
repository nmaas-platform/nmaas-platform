import { Route } from '@angular/router';
import { AppMarketComponent } from './appmarket.component';
import { AppListComponent } from './applist/applist.component';
import { AppDetailsComponent } from './appdetails/index';

import { AuthGuard } from '../auth/auth.guard';

import { AppListRoutes } from './applist/applist.routes';
import { AppInstanceRoutes } from './appinstance/appinstance.routes';
import { DomainsRoutes } from './domains/domains.routes';
import { UsersRoutes } from './users/users.routes';

export const AppMarketRoutes: Route[] = [
    {
      path: '',
      component: AppMarketComponent,
      canActivate: [AuthGuard],
      canActivateChild: [AuthGuard],
      children: [
        ...AppListRoutes,
        ...AppInstanceRoutes,
        ...DomainsRoutes,
        ...UsersRoutes,
          { path: 'apps/:id', component: AppDetailsComponent },

      ]
    }
];
