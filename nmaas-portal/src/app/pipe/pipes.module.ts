import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {AuthModule} from '../auth/auth.module';

import {GroupPipe, SecurePipe, KeysPipe, AuthHttpWrapper} from './index';

@NgModule({
  declarations: [GroupPipe, SecurePipe, KeysPipe],
  imports: [CommonModule, AuthModule],
  exports: [GroupPipe, SecurePipe, KeysPipe],
  providers: [AuthHttpWrapper]
})
export class PipesModule {}
