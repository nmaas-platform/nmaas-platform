import { NgModule }       from '@angular/core';
import { FormsModule }    from '@angular/forms';
import { CommonModule }   from '@angular/common';

import { CommentsComponent } from './comments/comments.component';
import { FooterComponent } from './footer/footer.component';
import { RateComponent } from './rate/rate.component';
import { ScreenshotsComponent } from './screenshots/screenshots.component';
import { ModalComponent } from "./modal/modal.component";
import { PipesModule } from '../pipe/pipes.module';

@NgModule({
    imports: [
        CommonModule,
        PipesModule,
        FormsModule
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