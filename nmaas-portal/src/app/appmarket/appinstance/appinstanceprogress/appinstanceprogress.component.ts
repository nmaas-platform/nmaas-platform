import {Component, Input, OnInit} from '@angular/core';

import {AppInstanceProgressStage, AppInstanceState} from '../../../model';
import {TranslateService} from '@ngx-translate/core';

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

    @Input()
    previousState: AppInstanceState = AppInstanceState.UNKNOWN;

    constructor(private translate: TranslateService) { }

    ngOnInit() {

    }

    getTranslateTag(stateProgress): string {
        stateProgress = stateProgress.toString().toUpperCase().split(' ').join('_');
        return this.translate.instant('APP_INSTANCE.PROGRESS.' + stateProgress.toString());
    }

    // future not failed state
    public displaySuccessStyle(stage: AppInstanceProgressStage): boolean {
        return (stage.activeState < this.activeState && this.activeState !== AppInstanceState.FAILURE)
            || (stage.activeState < this.previousState && this.previousState !== AppInstanceState.UNKNOWN)
    }

    // current not failed state
    public displayPrimaryStyle(stage: AppInstanceProgressStage): boolean {
        return stage.activeState === this.activeState && this.activeState !== AppInstanceState.FAILURE;
    }

    // failed or removed app instance
    public displayDangerStyle(stage: AppInstanceProgressStage): boolean {
        return this.activeState === AppInstanceState.FAILURE
            && stage.activeState === this.previousState
            || this.activeState === AppInstanceState.REMOVED
    }

    public displayDefaultStyle(stage: AppInstanceProgressStage): boolean {
        return (stage.activeState > this.previousState && this.previousState !== AppInstanceState.UNKNOWN )
            || (stage.activeState > this.activeState && this.activeState !== AppInstanceState.FAILURE)
            || (this.activeState === AppInstanceState.UNKNOWN)
            || (stage.activeState === AppInstanceState.REQUESTED)
            || (!stage?.activeState);
    }

}
