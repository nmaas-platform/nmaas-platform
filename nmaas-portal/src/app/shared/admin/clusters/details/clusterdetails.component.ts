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

    controllerConfigOption:Map<string,IngressControllerConfigOption> = new Map<string, IngressControllerConfigOption>();

    resourceConfigOption:Map<string, IngressResourceConfigOption> = new Map<string, IngressResourceConfigOption>();

    @Input()
    public cluster: Cluster = new Cluster();

    @Input()
    public error:string;

    @Output()
    public onSave: EventEmitter<Cluster> = new EventEmitter<Cluster>();

    @Output()
    public onDelete: EventEmitter<string> = new EventEmitter<string>();

    constructor(private router:Router) {
        super();
        this.initializeMaps();
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

    public getKeys(map){
        return Array.from(map.keys());
    }

    private initializeMaps(){
        this.resourceConfigOption.set('Do nothing',IngressResourceConfigOption.NOT_USED);
        this.resourceConfigOption.set('Deploy new or update resource using Kubernetes REST API', IngressResourceConfigOption.DEPLOY_USING_API);
        this.resourceConfigOption.set('Deploy new resource from the definition in the application chart', IngressResourceConfigOption.DEPLOY_FROM_CHART);
        this.controllerConfigOption.set('Use existing',IngressControllerConfigOption.USE_EXISTING);
        this.controllerConfigOption.set('Deploy new controller from chart repository', IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
        this.controllerConfigOption.set('Deploy new controller from local chart archive',IngressControllerConfigOption.DEPLOY_NEW_FROM_ARCHIVE);
    }
}