import {
    Cluster,
    ClusterExtNetwork,
    IngressControllerConfigOption,
    IngressResourceConfigOption
} from '../../../../model/cluster';
import { BaseComponent } from '../../../common/basecomponent/base.component';
import {ComponentMode} from '../../../common/componentmode';
import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';
import {Router} from "@angular/router";

@Component({
    selector: 'nmaas-clusterdetails',
    templateUrl: './clusterdetails.component.html',
    styleUrls: ['./clusterdetails.component.css']
})
export class ClusterDetailsComponent extends BaseComponent implements OnInit {

    controllerConfigOption = Object.keys(IngressControllerConfigOption).filter(value => typeof IngressControllerConfigOption[value] === "number");

    resourceConfigOption = Object.keys(IngressResourceConfigOption).filter(value => typeof IngressResourceConfigOption[value] === "number");

    @Input()
    public cluster: Cluster = new Cluster();

    @Output()
    public onSave: EventEmitter<Cluster> = new EventEmitter<Cluster>();

    @Output()
    public onDelete: EventEmitter<string> = new EventEmitter<string>();

    constructor(private router:Router) {
        super();
    }

    ngOnInit() {

    }

    public submit(): void {
        this.onSave.emit(this.cluster);
    }

    public remove(clusterName: string) {
        this.onDelete.emit(clusterName);
    }

    public onModeChange(): void {
        const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
        if (this.isModeAllowed(newMode)) {
            this.mode = newMode;
            if(this.mode === ComponentMode.VIEW){
                this.router.navigate(['admin/clusters'])
            }
        }
    }

    public removeNetwork(id) {
        this.cluster.externalNetworks.splice(
            this.cluster.externalNetworks.findIndex(
                function(i){
                    return i.id = id;
                }), 1);
    }

    public addNetwork() {
        let newobj: ClusterExtNetwork= new ClusterExtNetwork();
        this.cluster.externalNetworks.push(newobj);
    }

    public trackByFn(index) {
        return index;
    }
}