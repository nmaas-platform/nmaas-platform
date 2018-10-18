import { Route } from '@angular/router';
import { UsersListComponent, UserDetailsComponent } from './index';
import { AuthGuard } from '../../auth/auth.guard';
import { RoleGuard } from '../../auth/role.guard';
import { ComponentMode } from '../../shared/common/componentmode';

export const UsersRoutes: Route[] = [
    { path: 'users', component: UsersListComponent, canActivate: [AuthGuard, RoleGuard],
                      data: {roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_DOMAIN_ADMIN']}},
    { path: 'users/view/:id', component: UserDetailsComponent, canActivate: [AuthGuard, RoleGuard],
                      data: {mode: ComponentMode.VIEW, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_DOMAIN_ADMIN']} }
];
