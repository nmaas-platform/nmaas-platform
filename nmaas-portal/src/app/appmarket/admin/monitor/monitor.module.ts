import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MonitorDetailsComponent } from './details/monitor-details.component';
import { MonitorListComponent } from './list/monitor-list.component';
import {RouterModule} from "@angular/router";
import {ScheduleService} from "../../../service/schedule.service";
import {MonitorService} from "../../../service/monitor.service";
import {AuthModule} from "../../../auth/auth.module";
import {PipesModule} from "../../../pipe/pipes.module";
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../../../shared";

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        AuthModule,
        PipesModule,
        FormsModule,
        SharedModule
    ],
    providers: [
        ScheduleService,
        MonitorService
    ],

    declarations: [MonitorDetailsComponent, MonitorListComponent]
})
export class MonitorModule { }
