import {ClusterInfo} from '../../../../model/cluster';
import {BaseComponent} from '../../../common/basecomponent/base.component';
import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import 'rxjs/add/operator/shareReplay';
import 'rxjs/add/operator/take';

@Component({
    selector: 'nmaas-clusterlist',
    templateUrl: './clusterlist.component.html',
    styleUrls: ['./clusterlist.component.css']
})
export class ClusterListComponent extends BaseComponent implements OnInit {

    @Input()
    clusters: ClusterInfo[] = [];


    @Output()
    onDelete: EventEmitter<string> = new EventEmitter<string>();

    @Output()
    onView: EventEmitter<string> = new EventEmitter<string>();

    constructor() {
        super();
    }

    ngOnInit() {}

    protected remove(clusterName: string) {
        this.onDelete.emit(clusterName);
    }

    protected view(clusterName: string): void {
        this.onView.emit(clusterName);
    }
}
