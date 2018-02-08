import { Route } from '@angular/router';
import { AppListComponent } from './index';
import { AuthGuard } from '../../auth/auth.guard';

export const AppListRoutes: Route[] = [
    {
      path: '',
      component: AppListComponent,
      canActivate: [AuthGuard]
    }
];
