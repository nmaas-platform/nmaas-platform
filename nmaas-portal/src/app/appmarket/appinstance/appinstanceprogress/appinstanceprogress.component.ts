import { Component, OnInit, Input } from '@angular/core';

import { AppInstanceState, AppInstanceProgressStage } from '../../../model/index';

@Component({
    selector: 'nmaas-appinstanceprogress',
    templateUrl: './appinstanceprogress.component.html',
    styleUrls: ['./appinstanceprogress.component.css']
})
export class AppInstanceProgressComponent implements OnInit {

    @Input()
    stages: AppInstanceProgressStage[];

    @Input()
    public activeState: AppInstanceState = AppInstanceState.UNKNOWN;

    private appInstanceStateEnum = AppInstanceState;

    constructor() { }

    ngOnInit() {

    }

    public setState(appInstanceState: AppInstanceState): void {
        console.debug('Set progress state: ' + appInstanceState);
        this.activeState = appInstanceState;
    }

    public isSuccess(stage: AppInstanceProgressStage):boolean {
        return ( stage.activeState > AppInstanceState.SUBSCRIBED && stage.activeState < this.activeState)
    }
    
    public isCurrent(stage: AppInstanceProgressStage):boolean {
        return ( stage.activeState == this.activeState);
    }
    
    public isFailed(stage: AppInstanceProgressStage): boolean {
        return (this.activeState == AppInstanceState.FAILURE);
    }
    
}