import {Component, OnInit} from '@angular/core';
import {AppsService} from "../../../service";
import {Application} from "../../../model";
import {Router} from "@angular/router";
import {ApplicationState} from "../../../model/applicationstate";
import {AuthService} from "../../../auth/auth.service";

@Component({
  selector: 'nmaas-appmanagementlist',
  templateUrl: './appmanagementlist.component.html',
  styleUrls: ['./appmanagementlist.component.css']
})
export class AppManagementListComponent implements OnInit {

  public apps:Application[] = [];
  public newApps:Application[] = [];
  public rejectedApps:Application[] = [];

  constructor(public appsService:AppsService, public router:Router, public authService: AuthService) { }

  ngOnInit() {
    this.appsService.getAllApps().subscribe(val => {
      this.apps = val.filter(app => this.getStateAsString(app.state) != ApplicationState[ApplicationState.NEW].toString() && this.getStateAsString(app.state) != ApplicationState[ApplicationState.REJECTED].toString());
      this.newApps = val.filter(app => this.getStateAsString(app.state) === ApplicationState[ApplicationState.NEW].toString());
      this.rejectedApps = val.filter(app => this.getStateAsString(app.state) === ApplicationState[ApplicationState.REJECTED].toString());
    });
  }

  public removeApp(appId: number): void {
    this.appsService.deleteApp(appId).subscribe(() => {
      console.debug("App deleted");
      this.apps.find(app => app.id === appId).state = ApplicationState.DELETED;
    });
  }

  public getStateAsString(state: any): string {
    return typeof state === "string"? state: ApplicationState[state];
  }

  public changeApplicationState(appId: number, state: any): void {
    this.appsService.changeApplicationState(appId, this.getStateAsString(state)).subscribe(()=> {
      console.debug("App state changed");
      this.apps.find(app => app.id === appId).state = state;
    })
  }

}
