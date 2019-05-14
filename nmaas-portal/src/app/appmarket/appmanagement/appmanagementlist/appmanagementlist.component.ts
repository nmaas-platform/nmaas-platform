import {Component, OnInit, ViewChild} from '@angular/core';
import {AppsService} from "../../../service";
import {Application} from "../../../model";
import {Router} from "@angular/router";
import {ApplicationState} from "../../../model/applicationstate";
import {AuthService} from "../../../auth/auth.service";
import {AppChangeStateModalComponent} from "../appchangestatemodal/appchangestatemodal.component";
import {interval} from "rxjs";
import {ApplicationVersion} from "../../../model/applicationversion";

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

  public apps:Application[] = [];
  public newApps:Application[] = [];
  public rejectedApps:Application[] = [];
  public allApps:Application[] = [];
  public intervalChecker;

  constructor(public appsService:AppsService, public router:Router, public authService: AuthService) { }

  ngOnInit() {
    this.appsService.getAllApps().subscribe(val => {
      this.allApps = val;
      this.apps = val.filter(app => app.appVersions.filter(version => this.getStateAsString(version.state) != this.getStateAsString(ApplicationState.NEW) && this.getStateAsString(version.state) != this.getStateAsString(ApplicationState.REJECTED)).length > 0);
      this.newApps = val.filter(app => app.appVersions.filter(version => this.getStateAsString(version.state) === this.getStateAsString(ApplicationState.NEW)).length > 0);
      this.rejectedApps = val.filter(app => app.appVersions.filter(version => this.getStateAsString(version.state) === this.getStateAsString(ApplicationState.REJECTED)).length > 0);
    });
    this.intervalChecker = interval(5000).subscribe(() => this.filterApps());
  }

  public filterApps(): void {
      this.apps = this.allApps.filter(app => app.appVersions.filter(version => this.getStateAsString(version.state) != this.getStateAsString(ApplicationState.NEW) && this.getStateAsString(version.state) != this.getStateAsString(ApplicationState.REJECTED)).length > 0);
      this.newApps = this.allApps.filter(app => app.appVersions.filter(version => this.getStateAsString(version.state) === this.getStateAsString(ApplicationState.NEW)).length > 0);
      this.rejectedApps = this.allApps.filter(app => app.appVersions.filter(version => this.getStateAsString(version.state) === this.getStateAsString(ApplicationState.REJECTED)).length > 0);
  }

  public getStateAsString(state: any): string {
    return typeof state === "string" && isNaN(Number(state.toString())) ? state: ApplicationState[state];
  }

  public showModal(app:Application, appVersion: ApplicationVersion) : void {
    this.selectedAppName = app.name;
    this.selectedVersion = appVersion;
    this.modal.show();
  }

  public clickTableRow(app: Application){
    app.rowWithVersionVisible = !app.rowWithVersionVisible;
  }

}
