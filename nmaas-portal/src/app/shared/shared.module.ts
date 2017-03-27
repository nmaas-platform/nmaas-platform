import { NgModule }       from '@angular/core';
import { CommonModule }   from '@angular/common';

import { FooterComponent } from "./footer/footer.component";
import { RateComponent } from "./rate/rate.component";
import { CommentsComponent } from "./comments/comments.component";


@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
      RateComponent,
      FooterComponent,
      CommentsComponent

    ],
    providers: [
    ],
    exports: [
      RateComponent,
      FooterComponent,
      CommentsComponent
    ]
})
export class SharedModule {}