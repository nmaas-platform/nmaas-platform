import { Route } from '@angular/router';
import { GitlabDetailsComponent } from './index';
import { AuthGuard } from '../../../auth/auth.guard';
import { RoleGuard } from '../../../auth/role.guard';
import { ComponentMode } from '../../../shared';

export const GitlabRoutes: Route[] = [
    { path: 'admin/gitlab', component: GitlabDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}},
    { path: 'admin/gitlab/view', component: GitlabDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}}
];