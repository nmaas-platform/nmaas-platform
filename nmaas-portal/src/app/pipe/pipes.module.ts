import { NgModule } from '@angular/core';
import { CommonModule } from "@angular/common";

import { AuthModule } from "../auth/auth.module";

import { GroupPipe, SecurePipe, AuthHttpWrapper } from "./index";

@NgModule({
  declarations:[ GroupPipe, SecurePipe ],
  imports:[ CommonModule, AuthModule ],
  exports:[ GroupPipe, SecurePipe ],
  providers: [ AuthHttpWrapper ]
})
export class PipesModule{}