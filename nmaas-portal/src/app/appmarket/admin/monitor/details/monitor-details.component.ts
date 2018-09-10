import {Component, OnInit} from '@angular/core';
import {MonitorService} from "../../../../service/monitor.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MonitorEntry, ServiceType, TimeFormat} from "../../../../model/monitorentry";
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {ComponentMode} from "../../../../shared";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'nmaas-monitordetails',
  templateUrl: './monitor-details.component.html',
  styleUrls: ['./monitor-details.component.css']
})
export class MonitorDetailsComponent extends BaseComponent implements OnInit {

  private keys: any = Object.keys;

  private services: typeof ServiceType = ServiceType;

  private formats: typeof TimeFormat = TimeFormat;

  private monitorEntry: MonitorEntry;

  private errMsg: string;

  constructor(private monitorService: MonitorService, private router: Router, private route: ActivatedRoute) {
    super();
  }

  ngOnInit() {
    this.mode = this.getMode(this.route);
    this.route.params.subscribe(params =>{
      if(!isNullOrUndefined(params['name'])){
        this.monitorService.getOneMonitorEntry(params['name']).subscribe(entry => {
          this.monitorEntry = entry;
          if(entry.timeFormat === 0){
            this.monitorEntry.timeFormat = TimeFormat.H;
          } else{
            this.monitorEntry.timeFormat = TimeFormat.MIN;
          }
        });
      } else{
        this.monitorEntry = new MonitorEntry();
      }
    });
  }

  public submit():void{
    if(this.isInMode(ComponentMode.CREATE)){
      this.monitorService.createMonitorEntryAndJob(this.monitorEntry).subscribe(val => this.router.navigate(["/admin/monitor"]), err => this.errMsg = err.message);
    } else if(this.isInMode(ComponentMode.EDIT)){
      this.monitorService.updateMonitorEntryAndJob(this.monitorEntry).subscribe(val => this.router.navigate(["/admin/monitor"]), err => this.errMsg = err.message);
    }
  }

}
