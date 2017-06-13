import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AppInstanceProgressComponent } from './index';
import { AppInstanceState, AppInstanceProgressStage } from '../../../model/index';

@NgModule({
    declarations: [
        AppInstanceProgressComponent,
    ],
    imports: [ 
        CommonModule,
    ],
    exports: [
        AppInstanceProgressComponent,
    ],
    providers: [
    ]
    
})
export class AppInstanceProgressModule { }