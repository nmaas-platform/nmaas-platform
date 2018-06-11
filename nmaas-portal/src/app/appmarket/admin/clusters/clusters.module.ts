import {CommonModule} from "@angular/common";
import {RouterModule} from "@angular/router";
import {AuthModule} from "../../../auth/auth.module";
import {UserService} from "../../../service/user.service";
import {PipesModule} from "../../../pipe/pipes.module";
import {FormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";
import {SharedModule} from "../../../shared/shared.module";
import {ClusterListComponent} from "./list/clusterlist.component";
import {ClusterDetailsComponent} from "./details/clusterdetails.component";
import {ClusterService} from "../../../service/cluster.service";

@NgModule({
    declarations: [
        ClusterListComponent,
        ClusterDetailsComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        SharedModule,
        AuthModule,
        PipesModule,
    ],
    providers: [
        ClusterService,
    ]

})
export class ClustersModule {}