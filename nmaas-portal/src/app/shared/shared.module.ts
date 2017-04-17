import { NgModule }       from '@angular/core';
import { CommonModule }   from '@angular/common';

import { CommentsComponent, FooterComponent, RateComponent, ScreenshotsComponent, ModalComponent } from "./index";

@NgModule({
    imports: [
        CommonModule
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