import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AuthModule} from '../../auth/auth.module';
import {SharedModule} from '../../shared/shared.module';
import {PipesModule} from '../../pipe/pipes.module';

import {AppInstanceComponent} from './appinstance/appinstance.component';
import {AppInstanceProgressModule} from './appinstanceprogress/appinstanceprogress.module';

import {AppsService} from '../../service/apps.service';
import {AppInstanceService} from '../../service/appinstance.service';
import {TagService} from '../../service/tag.service';
import {AppInstanceListComponent} from './appinstancelist/appinstancelist.component';
import {AppRestartModalComponent} from "../modals/apprestart";
import {TranslateModule} from '@ngx-translate/core';
import {NgxPaginationModule} from "ngx-pagination";
import {FormioAppConfig, FormioModule} from "angular-formio";
import {AppConfig} from "../../../formio-config";
import {TooltipModule} from "ng2-tooltip-directive";

@NgModule({
  declarations: [
    AppInstanceComponent,
    AppInstanceListComponent,
      AppRestartModalComponent
  ],
  imports: [
    FormioModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    SharedModule,
    AuthModule,
    AppInstanceProgressModule,
    PipesModule,
    NgxPaginationModule,
    TranslateModule.forChild(),
    TooltipModule
  ],
  exports: [
    AppInstanceComponent,
    AppInstanceListComponent
  ],
  providers: [
    AppsService,
    AppInstanceService,
    TagService,
    {provide: FormioAppConfig, useValue: AppConfig}
  ]

})
export class AppInstanceModule {}
