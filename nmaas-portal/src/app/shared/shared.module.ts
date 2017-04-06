import { NgModule }       from '@angular/core';
import { CommonModule }   from '@angular/common';

import { CommentsComponent, FooterComponent, RateComponent, ScreenshotsComponent } from "./index";

@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
      RateComponent,
      FooterComponent,
      CommentsComponent,
      ScreenshotsComponent

    ],
    providers: [
    ],
    exports: [
      RateComponent,
      FooterComponent,
      CommentsComponent,
      ScreenshotsComponent
    ]
})
export class SharedModule {}