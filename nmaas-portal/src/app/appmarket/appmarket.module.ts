import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {AuthModule} from '../auth/auth.module';

import {AppMarketComponent} from './appmarket.component';
import {AppListModule} from './applist/applist.module';
import {AppDetailsComponent} from './appdetails';
import {AppInstanceModule} from './appinstance/appinstance.module';

import {SharedModule} from '../shared';

import {AppsService, DomainService, TagService, UserService} from '../service';

import {PipesModule} from '../pipe/pipes.module';
import {DomainsModule} from './domains/domains.module';
import {UsersModule} from './users/users.module';
import {ClustersModule} from './admin/clusters/clusters.module';
import {ClusterService} from '../service/cluster.service';
import {ConfigurationModule} from './admin/configuration/configuration.module';
import {MonitorModule} from './admin/monitor/monitor.module';
import {StorageServiceModule} from 'ngx-webstorage-service';
import {TranslateModule} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import {BrowserModule} from '@angular/platform-browser';
import {SortService} from '../service/sort.service';
import {AppManagementModule} from './appmanagement/app-management.module';
import {SessionService} from '../service/session.service';
import {LanguageManagementModule} from './admin/languagemanagement/languagemanagement.module';
import {TooltipModule} from 'ng2-tooltip-directive';
import { ModalGuestUserComponent } from './modals/modal-guest-user/modal-guest-user.component';

@NgModule({
  declarations: [
    AppMarketComponent,
    AppDetailsComponent,
    ModalGuestUserComponent,
  ],
  imports: [
    FormsModule,
    StorageServiceModule,
    CommonModule,
    RouterModule,
    SharedModule,
    AppListModule,
    AppInstanceModule,
    AppManagementModule,
    LanguageManagementModule,
    DomainsModule,
    UsersModule,
    AuthModule,
    PipesModule,
    ClustersModule,
    MonitorModule,
    ConfigurationModule,
    BrowserModule,
    HttpClientModule,
    TooltipModule,
    TranslateModule.forChild(),
  ],
  exports: [
    AppMarketComponent,
  ],
  providers: [
    AppsService,
    DomainService,
    UserService,
    TagService,
    UserService,
    ClusterService,
    SortService,
    SessionService,
  ]

})
export class AppMarketModule {}
