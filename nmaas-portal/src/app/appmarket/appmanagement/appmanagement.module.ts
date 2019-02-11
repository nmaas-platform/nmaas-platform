import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {SharedModule} from "../../shared";
import {AuthModule} from "../../auth/auth.module";
import {PipesModule} from "../../pipe/pipes.module";
import {TranslateModule} from "@ngx-translate/core";
import {AppsService} from "../../service";
import { AppManagementListComponent } from './appmanagementlist/appmanagementlist.component';

@NgModule({
  declarations: [ AppManagementListComponent ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    SharedModule,
    AuthModule,
    PipesModule,
    TranslateModule.forChild(),
  ],
  exports: [],
  providers: [AppsService]
})
export class AppManagementModule { }
