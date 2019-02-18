import {Route} from "@angular/router";
import {AuthGuard} from "../../auth/auth.guard";
import {RoleGuard} from "../../auth/role.guard";
import {AppManagementListComponent} from "./appmanagementlist/appmanagementlist.component";
import {AppCreateWizardComponent} from "./appcreatewizard/appcreatewizard.component";
import {ComponentMode} from "../../shared";

export const AppManagementRoutes: Route[] = [
    { path: 'management/apps', component: AppManagementListComponent, canActivate: [AuthGuard, RoleGuard], data: {roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER']}},
    { path: 'management/apps/create', component: AppCreateWizardComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER'], mode: ComponentMode.CREATE}},
    { path: 'management/apps/edit/:id', component: AppCreateWizardComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER'], mode: ComponentMode.EDIT}}
];