import {Route} from "@angular/router";
import {MonitorDetailsComponent} from "./details/monitor-details.component";
import {AuthGuard} from "../../../auth/auth.guard";
import {RoleGuard} from "../../../auth/role.guard";
import {MonitorListComponent} from "./list/monitor-list.component";
import {ComponentMode} from "../../../shared";

export const MonitorRoutes: Route[] = [
    {path: 'admin/monitor', component: MonitorListComponent, canActivate: [AuthGuard, RoleGuard],
        data: {roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}},
    {path: 'admin/monitor/add', component: MonitorDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.CREATE, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}},
    {path: 'admin/monitor/edit/:name', component: MonitorDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.EDIT, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}},
    {path: 'admin/monitor/view/:name', component: MonitorDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_OPERATOR']}}
];

