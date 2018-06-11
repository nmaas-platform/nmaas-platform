import {Component} from "@angular/core";
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {OnInit} from "@angular/core/public_api";
import {Cluster} from "../../../../model/cluster";
import {ClusterService} from "../../../../service/cluster.service";
import {isUndefined} from "util";
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'nmaas-clusterdetails',
    templateUrl: './clusterdetails.component.html',
    styleUrls: ['./clusterdetails.component.css']
})
export class ClusterDetailsComponent extends BaseComponent implements OnInit{
    private clusterName: string;
    private cluster: Cluster;

    constructor(private clusterService: ClusterService, private route: ActivatedRoute) {
        super();
    }

    ngOnInit() {
        this.route.params.subscribe(params => {
            if (!isUndefined(params['name'])) {
                this.clusterName = params['name'];
                this.clusterService.getOne(this.clusterName).subscribe((cluster) => this.cluster = cluster);
            }
        });
    }
}