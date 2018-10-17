import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ConfigurationService} from '../../../service';
import {ConfigurationDetailsComponent} from './details/configurationdetails.component';
import {RouterModule} from '@angular/router';
import {SharedModule} from '../../../shared';
import {AuthModule} from '../../../auth/auth.module';
import {PipesModule} from '../../../pipe/pipes.module';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

@NgModule({
    declarations: [
        ConfigurationDetailsComponent,
    ],
  imports: [
    CommonModule,
    RouterModule,
    SharedModule,
    AuthModule,
    FormsModule,
    PipesModule,
    TranslateModule.forChild()
  ],
  providers: [
      ConfigurationService,
  ]
})
export class ConfigurationModule { }
