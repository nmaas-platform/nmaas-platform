import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {JsonSchemaFormModule, Bootstrap3FrameworkModule } from 'angular2-json-schema-form';

import {AuthModule} from '../../auth/auth.module';
import {SharedModule} from '../../shared/shared.module';
import {PipesModule} from '../../pipe/pipes.module';

import {AppInstanceComponent} from './appinstance/appinstance.component';
import {AppInstanceProgressModule} from './appinstanceprogress/appinstanceprogress.module';

import {AppsService} from '../../service/apps.service';
import {AppInstanceService} from '../../service/appinstance.service';
import {TagService} from '../../service/tag.service';
import {AppInstanceListComponent} from './appinstancelist/appinstancelist.component';

@NgModule({
  declarations: [
    AppInstanceComponent,
    AppInstanceListComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    SharedModule,
    AuthModule,
    AppInstanceProgressModule,
    PipesModule,
    Bootstrap3FrameworkModule,
    JsonSchemaFormModule.forRoot(Bootstrap3FrameworkModule)
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
