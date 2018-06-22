import { Component, OnInit, AfterViewInit, Input, Output, ViewEncapsulation, ViewChildren, QueryList } from '@angular/core';

import { AppInstanceState, AppInstanceProgressStage } from '../../../model/index';

@Component({
    selector: 'nmaas-appinstanceprogress',
    templateUrl: './appinstanceprogress.component.html',
    styleUrls: ['./appinstanceprogress.component.css']
})
export class AppInstanceProgressComponent implements OnInit {

    public AppInstanceState = AppInstanceState;

    @Input()
    stages: AppInstanceProgressStage[]  = new Array<AppInstanceProgressStage>();


    @Input()
    activeState: AppInstanceState = AppInstanceState.UNKNOWN;
    
    constructor() { }

    ngOnInit() {

    }

    ngAfterViewInit() {
        // available here
    }

//    public getCurrentStage(): AppInstanceProgressStage {
//        console.debug('Test state: ' + this.activeState);
//        for(let stage of this.stages) {
//            if(this.activeState === stage.activeState) {
//                console.debug('Current stage:' + stage);
//                return stage; 
//            }
//        }
//        return null;
//    }
    
//    public setState(state: AppInstanceState): void {
//        console.debug('Set progress state: ' + state);
//        this.activeState = state;
//        for(let step of this.steps) {
//            if(this.activeState == AppInstanceState.FAILURE)
//                step.stepState = AppInstanceProgressStepState.FAILED;
//            else if(step.stage.activeState < this.activeState)
//                step.stepState = AppInstanceProgressStepState.SUCCESS;
//            else if(step.stage.activeState == this.activeState)
//                step.stepState = AppInstanceProgressStepState.ACTIVE;
//            else 
//                step.stepState = AppInstanceProgressStepState.DISABLED;
//        }
//            
//    }

}