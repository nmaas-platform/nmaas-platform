import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {PipesModule} from '../../pipe/pipes.module';

import {AuthModule} from '../../auth/auth.module';
import {SharedModule} from '../../shared/shared.module';

import {AppListComponent} from './applist.component';
import {AppElementComponent} from './appelement/appelement.component';



import {AppsService} from '../../service/apps.service';
import {TagService} from '../../service/tag.service';


@NgModule({
  declarations: [
    AppListComponent,
    AppElementComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    SharedModule,
    AuthModule,
    PipesModule,
  ],
  exports: [
    AppListComponent
  ],
  providers: [
    AppsService,
    TagService
  ]
})
export class AppListModule {}
