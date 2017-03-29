import { NgModule }       from '@angular/core';
import { CommonModule }   from '@angular/common';

import { FooterComponent } from "./footer/footer.component";
import { RateComponent } from "./rate/rate.component";
import { CommentsComponent } from "./comments/comments.component";
import { ScreenshotsComponent } from "./screenshots/screenshots.component";

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