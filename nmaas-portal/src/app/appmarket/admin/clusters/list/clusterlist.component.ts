import {Component, OnInit} from '@angular/core';
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {ClusterInfo} from "../../../../model/cluster";
import {ClusterService} from "../../../../service/cluster.service";
import {Router} from "@angular/router";

@Component({
    selector: 'nmaas-clusterlist',
    templateUrl: './clusterlist.component.html',
    styleUrls: ['./clusterlist.component.css']
})
export class ClusterListComponent extends BaseComponent implements OnInit {
    private clusters: ClusterInfo[] = [];

    constructor(private clusterService: ClusterService, private router: Router) {
        super();
    }

    ngOnInit() {
        this.clusterService.getAll().subscribe((clusters) => this.clusters=clusters);
    }

    public onView($event): void {
        this.router.navigate(['/admin/clusters/', $event]);
    }

    public onDelete($event): void {
        this.clusterService.remove($event);
    }
}
