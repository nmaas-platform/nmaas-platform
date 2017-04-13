import { NgModule } from '@angular/core';
import { RouterModule } from "@angular/router";
import { CommonModule } from '@angular/common';
import { FormsModule }   from '@angular/forms';

import { JsonSchemaFormModule } from 'angular2-json-schema-form/src';

import { AuthModule } from '../../auth/auth.module';
import { SharedModule } from '../../shared/shared.module';

import { AppInstanceComponent } from './appinstance/appinstance.component';
import { AppInstanceProgressComponent } from './appinstanceprogress/appinstanceprogress.component';

import { AppsService } from '../../service/apps.service';
import { AppInstanceService } from '../../service/appinstance.service'; 
import { TagService } from '../../service/tag.service';
import { AppInstanceListComponent } from './appinstancelist/appinstancelist.component';


@NgModule({
    declarations: [
        AppInstanceComponent,
        AppInstanceProgressComponent,
        AppInstanceListComponent
    ],
    imports: [ 
        CommonModule,
        FormsModule, 
        RouterModule,
        SharedModule,
        AuthModule,
        JsonSchemaFormModule.forRoot()
    ],
    exports: [
        AppInstanceComponent,
        AppInstanceListComponent
    ],
    providers: [
        AppsService,
        AppInstanceService,
        TagService
    ]
    
})
export class AppInstanceModule { }