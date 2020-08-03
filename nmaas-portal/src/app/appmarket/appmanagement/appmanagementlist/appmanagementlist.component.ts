import {Component, OnInit, ViewChild} from '@angular/core';
import {AppsService} from "../../../service";
import {Application} from "../../../model";
import {Router} from "@angular/router";
import {ApplicationState} from "../../../model/applicationstate";
import {AuthService} from "../../../auth/auth.service";
import {AppChangeStateModalComponent} from "../appchangestatemodal/appchangestatemodal.component";
import {ApplicationVersion} from "../../../model/applicationversion";
import {map} from "rxjs/operators";

@Component({
    selector: 'nmaas-appmanagementlist',
    templateUrl: './appmanagementlist.component.html',
    styleUrls: ['./appmanagementlist.component.css']
})
export class AppManagementListComponent implements OnInit {

    @ViewChild(AppChangeStateModalComponent)
    public modal: AppChangeStateModalComponent;

    public selectedAppName: string = "";
    public selectedVersion: ApplicationVersion = new ApplicationVersion();

    public apps: Application[] = [];

    constructor(public appsService: AppsService, public router: Router, public authService: AuthService) {
    }

    ngOnInit() {
        this.appsService.getAllApps().pipe(
            map(apps => {
                return apps
                    .map(app => {
                        // filter out deleted app versions
                        app.appVersions = app.appVersions.filter(appVersion => (typeof appVersion.state === 'string') ? appVersion.state !== 'DELETED' : appVersion.state !== ApplicationState.DELETED)
                        return app
                    })
                    // filter out deleted apps or apps with no versions
                    .filter(app => ((typeof app.state === 'string') ? app.state !== 'DELETED' : app.state !== ApplicationState.DELETED) && app.appVersions.length >= 1)
                    // sort by lowercase name
                    .sort((a, b) => {
                        if (a.name.toLowerCase() === b.name.toLowerCase()) {
                            return 0;
                        }
                        return (a.name.toLowerCase() > b.name.toLowerCase()) ? 1 : -1;
                    })
            })
        ).subscribe(val => {
            this.apps = val;
        });
    }

    public getStateAsString(state: any): string {
        return typeof state === "string" && isNaN(Number(state.toString())) ? state : ApplicationState[state];
    }

    public showModal(app: Application, appVersion: ApplicationVersion): void {
        this.selectedAppName = app.name;
        this.selectedVersion = appVersion;
        this.modal.show();
    }

    public clickTableRow(app: Application) {
        app.rowWithVersionVisible = !app.rowWithVersionVisible;
    }

    public isAnySubtableVisible(): boolean {
        return this.apps.filter(app => app.rowWithVersionVisible).length > 0;
    }

}
