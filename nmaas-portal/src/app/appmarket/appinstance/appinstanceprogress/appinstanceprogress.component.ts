import {Component, Input, OnInit} from '@angular/core';

import {AppInstanceProgressStage, AppInstanceState} from '../../../model/index';
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

}
