import { Component, OnInit } from '@angular/core';
import {AppsService} from "../../../service";
import {Application} from "../../../model";
import {Router} from "@angular/router";

@Component({
  selector: 'nmaas-appmanagementlist',
  templateUrl: './appmanagementlist.component.html',
  styleUrls: ['./appmanagementlist.component.css']
})
export class AppManagementListComponent implements OnInit {

  public apps:Application[] = [];

  constructor(public appsService:AppsService, public router:Router) { }

  ngOnInit() {
    this.appsService.getAllApps().subscribe(val => this.apps = val);
  }

  public removeApp(appId: number): void {
    this.appsService.deleteApp(appId).subscribe(() => {
      console.debug("App deleted");
      this.apps.find(app => app.id === appId).deleted = true;
    });
  }

}
