import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GitlabDetailsComponent } from './details/gitlab-details.component';
import {SharedModule} from "../../../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {AuthModule} from "../../../auth/auth.module";
import {PipesModule} from "../../../pipe/pipes.module";
import {GitlabService} from "../../../service/gitlab.service";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        SharedModule,
        AuthModule,
        PipesModule,
    ],
    declarations: [GitlabDetailsComponent],
    providers: [GitlabService,]
})
export class GitlabModule { }
