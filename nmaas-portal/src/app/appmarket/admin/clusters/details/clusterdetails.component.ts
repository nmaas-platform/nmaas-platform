import {Component} from "@angular/core";
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {OnInit} from "@angular/core/public_api";
import {Cluster} from "../../../../model/cluster";
import {ClusterService} from "../../../../service/cluster.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ComponentMode} from "../../../../shared";

@Component({
    selector: 'app-clusterdetails',
    templateUrl: './clusterdetails.component.html',
    styleUrls: ['./clusterdetails.component.css']
})
export class ClusterDetailsComponent extends BaseComponent implements OnInit{
    public clusterName: string;
    public cluster: Cluster;

    constructor(private clusterService: ClusterService, private route: ActivatedRoute, private router: Router) {
        super();
    }

    ngOnInit() {
        this.clusterService.getAll().subscribe(clusters => {
            if(clusters.length > 0){
                this.clusterName = clusters[0].name;
                this.clusterService.getOne(this.clusterName).subscribe(cluster => {
                    this.cluster = cluster;
                });
                this.router.navigate(['/admin/clusters/',this.clusterName])
            } else{
                this.cluster = new Cluster();
                this.mode = ComponentMode.CREATE;
            }
        });
    }

    public onSave($event) {
        const upCluster: Cluster = $event;
        if (!upCluster) return;
        if(this.clusterName) {
            this.clusterService.update(upCluster)
                .subscribe((e) => this.router.navigate(['/admin/clusters/']));
        } else {
            this.clusterService.add(upCluster)
                .subscribe((e) => this.router.navigate(['/admin/clusters/', upCluster.name]));
        }
    }

    public onDelete($event): void {
        this.clusterService.remove($event).subscribe(() => this.router.navigate(['/admin/clusters/']));
    }
}