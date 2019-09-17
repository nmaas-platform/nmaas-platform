import {Route} from "@angular/router";
import {LanguageListComponent} from "./languagelist/languagelist.component";
import {LanguageDetailsComponent} from "./languagedetails/languagedetails.component";
import {AuthGuard} from "../../../auth/auth.guard";
import {RoleGuard} from "../../../auth/role.guard";

export const LanguageManagementRoutes: Route[] = [
    {path: 'admin/languages', component: LanguageListComponent, canActivate: [AuthGuard, RoleGuard], data:{ roles: 'ROLE_SYSTEM_ADMIN'}},
    {path: 'admin/languages/:id', component: LanguageDetailsComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: 'ROLE_SYSTEM_ADMIN'}}
];