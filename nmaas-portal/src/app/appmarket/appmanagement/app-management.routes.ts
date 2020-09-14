import {Route} from "@angular/router";
import {AuthGuard} from "../../auth/auth.guard";
import {RoleGuard} from "../../auth/role.guard";
import {AppManagementListComponent} from "./app-management-list/appmanagementlist.component";
import {AppCreateWizardComponent} from "./app-create-wizard/appcreatewizard.component";
import {ComponentMode} from "../../shared";
import {AppPreviewComponent} from "./app-preview/apppreview.component";
import {AppVersionCreateWizardComponent} from "./app-version-create-wizard/appversioncreatewizard.component";

export const AppManagementRoutes: Route[] = [
    { path: 'admin/apps', component: AppManagementListComponent, canActivate: [AuthGuard, RoleGuard], data: {roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER']}},
    { path: 'admin/apps/create', component: AppCreateWizardComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER'], mode: ComponentMode.CREATE}},
    { path: 'admin/apps/create/version/:name', component: AppVersionCreateWizardComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER'], mode: ComponentMode.CREATE}},
    { path: 'admin/apps/edit/:id', component: AppCreateWizardComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER'], mode: ComponentMode.EDIT}},
    { path: 'admin/apps/edit/version/:id', component: AppVersionCreateWizardComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER'], mode: ComponentMode.EDIT}},
    { path: 'admin/apps/view/:id', component: AppPreviewComponent, canActivate: [AuthGuard, RoleGuard], data: {roles:['ROLE_SYSTEM_ADMIN', 'ROLE_TOOL_MANAGER']}}
];
