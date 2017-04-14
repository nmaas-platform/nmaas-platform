import { Component, OnInit } from '@angular/core';

import { AppInstance, AppInstanceState } from '../../../model/index';
import { AppsService, AppInstanceService } from '../../../service/index';

export enum AppInstanceListSelection {
    ALL,
    MY,
    USER
};

@Component({
    selector: 'nmaas-appinstancelist',
    templateUrl: './appinstancelist.component.html',
    styleUrls: ['./appinstancelist.component.css'],
    providers: [AppInstanceService, AppsService]
})
export class AppInstanceListComponent implements OnInit {

    private AppInstanceState: typeof AppInstanceState = AppInstanceState;
    
    private appInstances: AppInstance[];

    private listSelection: AppInstanceListSelection;

    private selectedUsername: string;

    constructor(private appInstanceService: AppInstanceService) { }

    ngOnInit() {
        if (!this.listSelection)
            this.listSelection = AppInstanceListSelection.MY;

        switch (+this.listSelection) {
            case AppInstanceListSelection.ALL: {
                this.appInstanceService.getAllAppInstances().subscribe(appInstances => this.appInstances = appInstances);
                break;
            }
            case AppInstanceListSelection.MY: {
                this.appInstanceService.getMyAppInstances().subscribe(appInstances => this.appInstances = appInstances);
                break
            }
            case AppInstanceListSelection.USER: {
                this.appInstanceService.getUserAppInstances(this.selectedUsername).subscribe(appInstances => this.appInstances = appInstances);
                break;
            }
        }
    }

}
