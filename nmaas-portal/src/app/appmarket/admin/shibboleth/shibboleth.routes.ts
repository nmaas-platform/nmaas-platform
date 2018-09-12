import {Route} from "@angular/router";
import {ShibbolethDetailsComponent} from "./details/shibboleth-details.component";
import {AuthGuard} from "../../../auth/auth.guard";
import {RoleGuard} from "../../../auth/role.guard";
import {ComponentMode} from "../../../shared";

export const ShibbolethRoutes: Route[] = [
    {path: 'admin/shibboleth', component: ShibbolethDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.VIEW, roles: ['ROLE_SUPERADMIN', 'ROLE_OPERATOR']}},
    {path: 'admin/shibboleth/:id', component: ShibbolethDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data: {mode: ComponentMode.EDIT, roles: ['ROLE_SUPERADMIN', 'ROLE_OPERATOR']}}
];