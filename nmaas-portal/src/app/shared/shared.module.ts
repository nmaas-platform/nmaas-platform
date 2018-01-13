import { NgModule }       from '@angular/core';
import { FormsModule }    from '@angular/forms';
import { CommonModule }   from '@angular/common';

import { CommentsComponent } from './comments/index';
import { FooterComponent } from './footer/index';
import { RateComponent } from './rate/index';
import { ScreenshotsComponent } from './screenshots/index';
import { ModalComponent } from './modal/index';
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