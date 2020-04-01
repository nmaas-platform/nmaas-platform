import { DefaultLogo } from '../directive/defaultlogo.directive';
import { RolesDirective } from '../directive/roles.directive';
import { NgModule } from '@angular/core';
import {FormsModule, MaxLengthValidator, ReactiveFormsModule} from '@angular/forms';
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
import { ClusterDetailsComponent } from './admin/clusters/details/clusterdetails.component';
import { GitlabDetailsComponent } from './admin/gitlab/details/gitlab-details.component';
import { ModalInfoTermsComponent } from './modal/modal-info-terms/modal-info-terms.component';
import { ModalInfoPolicyComponent } from './modal/modal-info-policy/modal-info-policy.component';
import { TranslateModule } from '@ngx-translate/core';
import { SortableColumnComponent } from './sortable-column/sortable-column.component';
import { SortableTableDirective } from './sortable-column/sortable-table.directive';
import { AppInstallModalComponent } from './modal/appinstall';
import { RatingExtendedComponent } from './rating-extended/rating-extended.component';
import { TooltipModule } from 'ng2-tooltip-directive';
import { PasswordStrengthMeterModule } from 'angular-password-strength-meter';
import { AboutComponent } from './about/about.component';
import {ChangelogComponent} from './changelog/changelog.component';
import {NotificationService} from '../service/notification.service';
import {RECAPTCHA_LANGUAGE, RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module} from 'ng-recaptcha';
import { SingleCommentComponent } from './comments/single-comment/single-comment.component';
import {TranslateStateModule} from './translate-state/translate-state.module';
import {MinLengthDirective} from '../directive/min-length.directive';
import {MaxLengthDirective} from '../directive/max-length.directive';
import {AppConfigService} from '../service';
import { ModalTestInstanceComponent } from './modal/modal-test-instance/modal-test-instance.component';
import { ModalNotificationSendComponent } from './modal/modal-notification-send/modal-notification-send.component';
import {NgxPaginationModule} from "ngx-pagination";
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {DomainRolesDirective} from "../directive/domain-roles.directive";

@NgModule({
    imports: [
        CommonModule,
        PipesModule,
        FormsModule,
        ServicesModule,
        RouterModule,
        ReactiveFormsModule,
        RecaptchaV3Module,
        PasswordStrengthMeterModule,
        TranslateModule.forChild(),
        TooltipModule,
        NgxPaginationModule
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
    MinLengthDirective,
    MaxLengthDirective,
    SearchComponent,
    TagFilterComponent,
    DomainFilterComponent,
    AppElementComponent,
    AppListComponent,
    AppInstallModalComponent,
    ApplicationsViewComponent,
    ClusterDetailsComponent,
    GitlabDetailsComponent,
    ModalInfoTermsComponent,
    ModalInfoPolicyComponent,
    SortableColumnComponent,
    SortableTableDirective,
    RatingExtendedComponent,
    AboutComponent,
    ChangelogComponent,
    SingleCommentComponent,
    ModalTestInstanceComponent,
    ModalNotificationSendComponent,
      DomainRolesDirective,
      PageNotFoundComponent,
  ],
  providers: [
    PasswordValidator,
    UserDataService,
    NotificationService,
    AppConfigService,
    {
      provide: RECAPTCHA_V3_SITE_KEY,
      useFactory: function (appConfigService: AppConfigService) {
          return appConfigService.getSiteKey();
      },
      deps: [AppConfigService]
    }
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
        AppInstallModalComponent,
        RolesDirective,
        MinLengthDirective,
        MaxLengthDirective,
        SearchComponent,
        TagFilterComponent,
        DomainFilterComponent,
        ApplicationsViewComponent,
        ClusterDetailsComponent,
        GitlabDetailsComponent,
        ModalInfoTermsComponent,
        ModalInfoPolicyComponent,
        SortableColumnComponent,
        SortableTableDirective,
        RatingExtendedComponent,
        AboutComponent,
        TranslateStateModule,
        ModalTestInstanceComponent,
        ModalNotificationSendComponent,
        DomainRolesDirective,
    ]
})
export class SharedModule {}
