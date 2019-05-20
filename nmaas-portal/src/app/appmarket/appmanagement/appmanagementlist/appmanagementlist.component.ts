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

  constructor(public appsService:AppsService, public router:Router, public authService: AuthService) { }

  ngOnInit() {
    this.appsService.getAllApps().subscribe(val => {
      this.apps = val;
    });
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
