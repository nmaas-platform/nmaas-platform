import {Cluster} from '../../../../model/cluster';
import { BaseComponent } from '../../../common/basecomponent/base.component';
import {ComponentMode} from '../../../common/componentmode';
import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';

@Component({
    selector: 'nmaas-clusterdetails',
    templateUrl: './clusterdetails.component.html',
    styleUrls: ['./clusterdetails.component.css']
})
export class ClusterDetailsComponent extends BaseComponent implements OnInit {

    @Input()
    public cluster: Cluster = new Cluster();

    @Output()
    public onSave: EventEmitter<Cluster> = new EventEmitter<Cluster>();

    constructor() {
        super();
    }

    ngOnInit() {

    }

    public submit(): void {
        this.onSave.emit(this.cluster);
    }

    public onModeChange(): void {
        const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
        if (this.isModeAllowed(newMode)) {
            this.mode = newMode;
        }
    }
}