import {Route} from "@angular/router";
import {AuthGuard} from "../../../auth/auth.guard";
import {RoleGuard} from "../../../auth/role.guard";
import {ConfigurationDetailsComponent} from "./index";

export const ConfigurationRoutes: Route[] = [
    {path: 'admin/configuration', component: ConfigurationDetailsComponent, canActivate: [AuthGuard, RoleGuard],
        data:{roles: ['ROLE_SYSTEM_ADMIN']} }
];
