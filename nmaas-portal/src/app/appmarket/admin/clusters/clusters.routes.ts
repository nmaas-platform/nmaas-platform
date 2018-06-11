import { Route } from '@angular/router';
import { ClusterDetailsComponent, ClusterListComponent } from './index';
import { AuthGuard } from '../../../auth/auth.guard';
import { RoleGuard } from '../../../auth/role.guard';
import { ComponentMode } from '../../../shared/common/componentmode';

export const ClustersRoutes: Route[] = [
    { path: 'admin/clusters', component: ClusterListComponent, canActivate: [AuthGuard, RoleGuard],
        data: {roles: ['ROLE_SUPERADMIN']}},
    { path: 'admin/clusters/:name', component: ClusterDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SUPERADMIN']} }
];
