import {Route} from '@angular/router';
import {DomainsListComponent, DomainComponent} from './index';
import {AuthGuard} from '../../auth/auth.guard';
import {RoleGuard} from '../../auth/role.guard';
import { ComponentMode } from '../../shared/common/componentmode';

export const DomainsRoutes: Route[] = [
  {path: 'domains', component: DomainsListComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_DOMAIN_ADMIN', 'ROLE_OPERATOR']}},
  {path: 'domains/add', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {mode: ComponentMode.CREATE, roles: ['ROLE_SYSTEM_ADMIN']}},
  {path: 'domains/view/:id', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_DOMAIN_ADMIN', 'ROLE_OPERATOR']}},
  {path: 'domains/edit/:id', component: DomainComponent, canActivate: [AuthGuard, RoleGuard],
                        data: {mode: ComponentMode.EDIT, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}}
  
];
