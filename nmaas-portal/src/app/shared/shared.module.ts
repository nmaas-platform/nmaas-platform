import { NgModule }       from '@angular/core';
import { CommonModule }   from '@angular/common';

import { CommentsComponent, FooterComponent, RateComponent, ScreenshotsComponent, ModalComponent } from "./index";
import { PipesModule } from '../pipe/pipes.module';

@NgModule({
    imports: [
        CommonModule,
        PipesModule
    ],
    declarations: [
      RateComponent,
      FooterComponent,
      CommentsComponent,
      ScreenshotsComponent,
      ModalComponent
    ],
    providers: [
    ],
    exports: [
      RateComponent,
      FooterComponent,
      CommentsComponent,
      ScreenshotsComponent,
      ModalComponent
    ]
})
export class SharedModule {}