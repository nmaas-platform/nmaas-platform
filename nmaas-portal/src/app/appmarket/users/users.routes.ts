import { Route } from '@angular/router';
import { UsersListComponent, UserDetailsComponent } from './index';
import { AuthGuard } from '../../auth/auth.guard';
import { RoleGuard } from '../../auth/role.guard';

export const UsersRoutes: Route[] = [
    { path: 'users', component: UsersListComponent, canActivate: [AuthGuard, RoleGuard],
                      data: {roles: ['ROLE_SUPERADMIN', 'ROLE_DOMAIN_ADMIN']}},
    { path: 'users/add', component: UserDetailsComponent, canActivate: [AuthGuard, RoleGuard],
                      data: {roles: ['ROLE_SUPERADMIN', 'ROLE_DOMAIN_ADMIN']} },
    { path: 'users/:id', component: UserDetailsComponent, canActivate: [AuthGuard, RoleGuard],
                      data: {roles: ['ROLE_SUPERADMIN', 'ROLE_DOMAIN_ADMIN']} }
];
