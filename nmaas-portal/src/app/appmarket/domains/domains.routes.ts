import {Route} from '@angular/router';
import {DomainsListComponent, DomainComponent} from './index';
import {AuthGuard} from '../../auth/auth.guard';
import {RoleGuard} from '../../auth/role.guard';
import { ComponentMode } from '../../shared/common/componentmode';

export const DomainsRoutes: Route[] = [
  {path: 'domains', component: DomainsListComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {roles: ['ROLE_SUPERADMIN', 'ROLE_DOMAIN_ADMIN']}},
  {path: 'domains/add', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {mode: ComponentMode.CREATE, roles: ['ROLE_SUPERADMIN']}},
  {path: 'domains/view/:id', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SUPERADMIN', 'ROLE_DOMAIN_ADMIN']}},
  {path: 'domains/edit/:id', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {mode: ComponentMode.EDIT, roles: ['ROLE_SUPERADMIN']}}
  
];
