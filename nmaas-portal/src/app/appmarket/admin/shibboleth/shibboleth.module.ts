import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShibbolethDetailsComponent } from './details/shibboleth-details.component';
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {SharedModule} from "../../../shared";
import {AuthModule} from "../../../auth/auth.module";
import {PipesModule} from "../../../pipe/pipes.module";
import {ShibbolethService} from "../../../service/shibboleth.service";

@NgModule({
  imports: [
      CommonModule,
      FormsModule,
      RouterModule,
      SharedModule,
      AuthModule,
      PipesModule,
  ],
  declarations: [ShibbolethDetailsComponent],
    providers:[ShibbolethService]
})
export class ShibbolethModule { }
