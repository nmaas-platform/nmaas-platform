import { Route } from '@angular/router';
import { AppInstanceComponent } from './index';
import { AppInstanceListComponent } from './index';
import { AuthGuard } from '../../auth/auth.guard';

export const AppInstanceRoutes: Route[] = [
    { path: 'instances', component: AppInstanceListComponent },
    { path: 'instances/:id', component: AppInstanceComponent }
];
