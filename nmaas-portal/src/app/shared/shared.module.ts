import { DefaultLogo } from '../directive/defaultlogo.directive';
import { RolesDirective } from '../directive/roles.directive';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { CommentsComponent } from './comments/index';
import { FooterComponent } from './footer/index';
import { RateComponent } from './rate/index';
import { ScreenshotsComponent } from './screenshots/index';
import { ModalComponent } from './modal/index';
import { PipesModule } from '../pipe/pipes.module';
import { ServicesModule } from '../service/services.module';
import { UserDataService } from '../service/userdata.service';
import { PasswordComponent, PasswordValidator } from './common/password/password.component';
import { UserDetailsComponent } from './users/details/userdetails.component';
import { UsersListComponent } from './users/list/userslist.component';
import { UserPrivilegesComponent } from './users/privileges/userprivileges.component';
import { BaseComponent } from './common/basecomponent/base.component';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from './navbar/index';
import { SearchComponent } from './common/search/search.component';
import { TagFilterComponent } from './common/tagfilter/tagfilter.component';
import { DomainFilterComponent } from './common/domainfilter/domainfilter.component';
import { AppListComponent } from './applications/list/applist.component';
import { ApplicationsViewComponent } from './applications/applications.component';
import { AppElementComponent } from './applications/list/element/appelement.component';
import { ClusterDetailsComponent } from "./admin/clusters/details/clusterdetails.component";
import {GitlabDetailsComponent} from "./admin/gitlab/details/gitlab-details.component";
import { ModalInfoTermsComponent } from './modal/modal-info-terms/modal-info-terms.component';
import { ModalInfoPolicyComponent } from './modal/modal-info-policy/modal-info-policy.component';
import { ModalChangelogComponent } from './footer/modal-changelog/modal-changelog.component';

@NgModule({
  imports: [
    CommonModule,
    PipesModule,
    FormsModule,
    ServicesModule,
    RouterModule,
    ReactiveFormsModule
  ],
  declarations: [
    RateComponent,
    FooterComponent,
    CommentsComponent,
    ScreenshotsComponent,
    ModalComponent,
    UserDetailsComponent,
    UsersListComponent,
    PasswordComponent,
    UserPrivilegesComponent,
    BaseComponent,
    DefaultLogo,
    NavbarComponent,
    DefaultLogo,
    RolesDirective,
    SearchComponent,
    TagFilterComponent,
    DomainFilterComponent,
    AppElementComponent,
    AppListComponent,
    ApplicationsViewComponent,
      ClusterDetailsComponent,
      GitlabDetailsComponent,
      ModalInfoTermsComponent,
      ModalInfoPolicyComponent,
      ModalChangelogComponent,
  ],
  providers: [
    PasswordValidator,
    UserDataService
  ],
  exports: [
    RateComponent,
    FooterComponent,
    CommentsComponent,
    ScreenshotsComponent,
    ModalComponent,
    UserDetailsComponent,
    UsersListComponent,
    PasswordComponent,
    UserPrivilegesComponent,
    NavbarComponent,
    UserPrivilegesComponent,
    RolesDirective,
    SearchComponent,
    TagFilterComponent,
    DomainFilterComponent,
    ApplicationsViewComponent,
    ClusterDetailsComponent,
      GitlabDetailsComponent,
      ModalInfoTermsComponent,
      ModalInfoPolicyComponent
  ]
})
export class SharedModule {}
