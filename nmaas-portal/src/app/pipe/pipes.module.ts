import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {AuthModule} from '../auth/auth.module';

import {GroupPipe, SecurePipe, KeysPipe, AuthHttpWrapper} from './index';
import { LocalDatePipe } from './local-date.pipe';

@NgModule({
  declarations: [GroupPipe, SecurePipe, KeysPipe, LocalDatePipe],
  imports: [CommonModule, AuthModule],
  exports: [GroupPipe, SecurePipe, KeysPipe, LocalDatePipe],
  providers: [AuthHttpWrapper]
})
export class PipesModule {}
