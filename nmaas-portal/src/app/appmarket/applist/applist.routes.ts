import { Route } from '@angular/router';
import { AppListComponent } from './applist.component';
import { AuthGuard } from '../../auth/auth.guard';
import { AppViewType } from '../../shared/common/viewtype';

export const AppListRoutes: Route[] = [
    {
      path: '',
      component: AppListComponent,
      canActivate: [AuthGuard],
      data: {
        appView: AppViewType.APPLICATION
      }
    },
    {
      path: 'subscriptions',
      component: AppListComponent,
      canActivate: [AuthGuard],
      data: {
        appView: AppViewType.DOMAIN
      }
    }    
];
