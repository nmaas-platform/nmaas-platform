import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {PipesModule} from '../../pipe/pipes.module';

import {AuthModule} from '../../auth/auth.module';
import {SharedModule} from '../../shared/shared.module';

import {DomainsListComponent} from './list/domainslist.component';
import {DomainComponent} from './domain/domain.component';

import {DomainService} from '../../service/domain.service';



@NgModule({
  declarations: [
    DomainsListComponent,
    DomainComponent,
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
    DomainsListComponent,
  ],
  providers: [
    DomainService,
  ]

})
export class DomainsModule {}
