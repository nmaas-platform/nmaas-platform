import {Route} from '@angular/router';
import {DomainsListComponent, DomainComponent} from './index';
import {AuthGuard} from '../../auth/auth.guard';
import {RoleGuard} from '../../auth/role.guard';

export const DomainsRoutes: Route[] = [
  {path: 'domains', component: DomainsListComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {roles: ['ROLE_SUPERADMIN', 'ROLE_DOMAIN_ADMIN']}},
  {path: 'domains/add', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {roles: ['ROLE_SUPERADMIN']}},
  {path: 'domains/:id', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {roles: ['ROLE_SUPERADMIN']}}
];
