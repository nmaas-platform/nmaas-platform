import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
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

@NgModule({
  declarations: [
    AppInstanceComponent,
    AppInstanceListComponent,
      AppRestartModalComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    SharedModule,
    AuthModule,
    AppInstanceProgressModule,
    PipesModule
  ],
  exports: [
    AppInstanceComponent,
    AppInstanceListComponent
  ],
  providers: [
    AppsService,
    AppInstanceService,
    TagService
  ]

})
export class AppInstanceModule {}
