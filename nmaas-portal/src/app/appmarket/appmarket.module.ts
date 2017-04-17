import { NgModule } from '@angular/core';
import { RouterModule } from "@angular/router";
import { CommonModule } from '@angular/common';
import { FormsModule }   from '@angular/forms';

import { AuthModule } from '../auth/auth.module';

import { AppMarketComponent } from './appmarket.component';
import { AppListModule } from './applist/applist.module';
import { AppDetailsComponent } from './appdetails/index';
import { AppInstanceModule } from './appinstance/appinstance.module';

import { NavbarComponent } from './navbar/index';
import { LogoutComponent } from '../logout/index';

import { SharedModule } from '../shared/shared.module';

import { AppsService } from '../service/apps.service';
import { TagService } from '../service/tag.service';
import { AppInstallModalComponent } from './appinstall/appinstallmodal.component';

import { GroupPipe } from '../pipe/group.pipe';

@NgModule({
    declarations: [
        AppMarketComponent,
        AppDetailsComponent,
        NavbarComponent,
        AppInstallModalComponent
    ],
    imports: [ 
        FormsModule,
        CommonModule,
        RouterModule,
        SharedModule,
        AppListModule,
        AppInstanceModule,
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