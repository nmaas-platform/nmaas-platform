import {Component, OnChanges, OnInit, SimpleChanges, ViewChild, ViewChildren} from '@angular/core';
import {AppsService} from "../../../service";
import {Application} from "../../../model";
import {Router} from "@angular/router";
import {ApplicationState} from "../../../model/applicationstate";
import {AuthService} from "../../../auth/auth.service";
import {AppChangeStateModalComponent} from "../appchangestatemodal/appchangestatemodal.component";
import {interval} from "rxjs";

@Component({
  selector: 'nmaas-appmanagementlist',
  templateUrl: './appmanagementlist.component.html',
  styleUrls: ['./appmanagementlist.component.css']
})
export class AppManagementListComponent implements OnInit {

  @ViewChild(AppChangeStateModalComponent)
  public modal: AppChangeStateModalComponent;

  public selectedApp:Application = new Application();

  public apps:Application[] = [];
  public newApps:Application[] = [];
  public rejectedApps:Application[] = [];
  public allApps:Application[] = [];
  public intervalChecker;

  constructor(public appsService:AppsService, public router:Router, public authService: AuthService) { }

  ngOnInit() {
    this.appsService.getAllApps().subscribe(val => {
      this.allApps = val;
      this.apps = val.filter(app => this.getStateAsString(app.state) != this.getStateAsString(ApplicationState.NEW) && this.getStateAsString(app.state) != this.getStateAsString(ApplicationState.REJECTED));
      this.newApps = val.filter(app => this.getStateAsString(app.state) === this.getStateAsString(ApplicationState.NEW));
      this.rejectedApps = val.filter(app => this.getStateAsString(app.state) === this.getStateAsString(ApplicationState.REJECTED));
    });
    this.intervalChecker = interval(5000).subscribe(() => this.filterApps());
  }

  public filterApps(): void {
      this.apps = this.allApps.filter(app => this.getStateAsString(app.state) != this.getStateAsString(ApplicationState.NEW) && this.getStateAsString(app.state) != this.getStateAsString(ApplicationState.REJECTED));
      this.newApps = this.allApps.filter(app => this.getStateAsString(app.state) === this.getStateAsString(ApplicationState.NEW));
      this.rejectedApps = this.allApps.filter(app => this.getStateAsString(app.state) === this.getStateAsString(ApplicationState.REJECTED));
  }

  public getStateAsString(state: any): string {
    return typeof state === "string" && isNaN(Number(state.toString())) ? state: ApplicationState[state];
  }

  public showModal(app:Application) : void {
    this.selectedApp = app;
    this.modal.show();
  }

}
