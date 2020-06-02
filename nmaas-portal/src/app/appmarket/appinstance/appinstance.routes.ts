import { Route } from '@angular/router';
import { AppInstanceComponent } from './index';
import { AppInstanceListComponent } from './index';
import { AuthGuard } from '../../auth/auth.guard';
import {AppInstanceShellViewComponent} from './appinstance-shell-view/appinstance-shell-view.component';

export const AppInstanceRoutes: Route[] = [
    { path: 'instances', component: AppInstanceListComponent },
    { path: 'instances/:id', component: AppInstanceComponent },
    { path: 'instances/:id/shell', component: AppInstanceShellViewComponent }
];
