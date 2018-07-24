import {Component, OnInit} from '@angular/core';
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {Router} from "@angular/router";
import {MaintenanceService} from "../../../../service";

@Component({
  selector: 'app-configurationdetails',
  templateUrl: './configurationdetails.component.html',
  styleUrls: ['./configurationdetails.component.css']
})
export class ConfigurationDetailsComponent extends BaseComponent implements OnInit {

  protected maintenance:boolean;

  constructor(private router:Router, private maintenanceService:MaintenanceService) {
    super();
    this.update();
  }

  ngOnInit() {
  }

  public update():void{
      this.maintenanceService.getMaintenance().subscribe(value => this.maintenance = value.maintenance);
  }

  public save():void{
      this.maintenanceService.setMaintenance(this.maintenance).subscribe(() => this.update());
  }


}
