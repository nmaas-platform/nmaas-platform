import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {AuthModule} from '../auth/auth.module';

import {AppMarketComponent} from './appmarket.component';
import {AppListModule} from './applist/applist.module';
import {AppDetailsComponent} from './appdetails/index';
import {AppInstanceModule} from './appinstance/appinstance.module';

import {NavbarComponent} from './navbar/index';

import {SharedModule} from '../shared/shared.module';

import {AppsService} from '../service/apps.service';
import {DomainService} from '../service/domain.service';
import {TagService} from '../service/tag.service';
import {UserService} from '../service/user.service';

import {PipesModule} from '../pipe/pipes.module';
import {DomainsModule} from './domains/domains.module';
import {UsersModule} from './users/users.module';
import {ClustersModule} from './admin/clusters/clusters.module';
import {ClusterService} from '../service/cluster.service';
import {GitlabModule} from './admin/gitlab/gitlab.module';
import {ConfigurationModule} from './admin/configuration/configuration.module';
import {MonitorModule} from './admin/monitor/monitor.module';
import {StorageServiceModule} from 'ngx-webstorage-service';
import {TranslateModule} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import {BrowserModule} from '@angular/platform-browser';
import {SortService} from "../service/sort.service";
import { AppCreateWizardComponent } from './appcreatewizard/appcreatewizard.component';
import {StepsModule} from "primeng/steps";
import {MultiSelectModule} from "primeng/primeng";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

@NgModule({
  declarations: [
    AppMarketComponent,
    AppDetailsComponent,
    NavbarComponent,
    AppCreateWizardComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    StorageServiceModule,
    CommonModule,
    RouterModule,
    SharedModule,
    AppListModule,
    AppInstanceModule,
    DomainsModule,
    UsersModule,
    AuthModule,
    PipesModule,
    ClustersModule,
    GitlabModule,
    MonitorModule,
    ConfigurationModule,
    BrowserModule,
    HttpClientModule,
    StepsModule,
    MultiSelectModule,
    BrowserAnimationsModule,
    TranslateModule.forChild(),
  ],
  exports: [
    AppMarketComponent,
      NavbarComponent
  ],
  providers: [
    AppsService,
    DomainService,
    UserService,
    TagService,
    UserService,
    ClusterService,
    SortService,
  ]

})
export class AppMarketModule {}
