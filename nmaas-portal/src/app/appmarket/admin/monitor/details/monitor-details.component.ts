import {Component, OnInit} from '@angular/core';
import {MonitorService} from "../../../../service/monitor.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MonitorEntry, ServiceType} from "../../../../model/monitorentry";
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {ComponentMode} from "../../../../shared";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'nmaas-monitordetails',
  templateUrl: './monitor-details.component.html',
  styleUrls: ['./monitor-details.component.css']
})
export class MonitorDetailsComponent extends BaseComponent implements OnInit {

  private services: typeof ServiceType = ServiceType;

  private monitorEntry: MonitorEntry;

  constructor(private monitorService: MonitorService, private router: Router, private route: ActivatedRoute) {
    super();
  }

  ngOnInit() {
    this.mode = this.getMode(this.route);
    this.route.params.subscribe(params =>{
      if(!isNullOrUndefined(params['name'])){
        this.monitorService.getOneMonitorEntry(params['name']).subscribe(entry => {
          this.monitorEntry = entry;
        });
      } else{
        this.monitorEntry = new MonitorEntry();
      }
    });
  }

  public submit():void{
    if(this.isInMode(ComponentMode.CREATE)){
      this.monitorService.createMonitorEntryAndJob(this.monitorEntry).subscribe(val => this.router.navigate(["/admin/monitor"]));
    } else if(this.isInMode(ComponentMode.EDIT)){
      this.monitorService.updateMonitorEntryAndJob(this.monitorEntry).subscribe(val => this.router.navigate(["/admin/monitor"]));
    }
  }

}
