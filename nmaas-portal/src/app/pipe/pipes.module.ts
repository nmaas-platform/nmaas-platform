import { NgModule } from '@angular/core';
import { CommonModule } from "@angular/common";

import { GroupPipe } from "./index";

@NgModule({
  declarations:[ GroupPipe ],
  imports:[ CommonModule ],
  exports:[ GroupPipe ]
})

export class PipesModule{}