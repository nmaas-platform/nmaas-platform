import { NgModule } from '@angular/core';
import { RouterModule } from "@angular/router";
import { CommonModule } from '@angular/common';

import { AuthModule } from '../auth/auth.module';

import { AppMarketComponent } from './appmarket.component';
import { AppListModule } from './applist/applist.module';
import { AppDetailsComponent } from './appdetails/index';
import { AppInstallComponent } from './appinstall/index';

import { NavbarComponent } from './navbar/index';
import { LogoutComponent } from '../logout/index';

import { SharedModule } from '../shared/shared.module';

import { AppsService } from '../service/apps.service';
import { TagService } from '../service/tag.service';


@NgModule({
    declarations: [
        AppMarketComponent,
        AppDetailsComponent,
        AppInstallComponent,
        NavbarComponent
    ],
    imports: [ 
        CommonModule,
        RouterModule,
        SharedModule,
        AppListModule,
        AuthModule
    ],
    exports: [
        AppMarketComponent
    ],
    providers: [
        AppsService,
        TagService
    ]
    
})
export class AppMarketModule { }