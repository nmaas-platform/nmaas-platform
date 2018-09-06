import {Component, OnInit} from '@angular/core';
import {ScheduleService} from "../../../../service/schedule.service";
import {MonitorService} from "../../../../service/monitor.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MonitorEntry, ServiceType} from "../../../../model/monitorentry";
import {JobDescriptor} from "../../../../model/jobdescriptor";
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

  private jobDescriptor: JobDescriptor = new JobDescriptor();

  constructor(private scheduleService: ScheduleService, private monitorService: MonitorService, private router: Router, private route: ActivatedRoute) {
    super();
  }

  ngOnInit() {
    this.mode = this.getMode(this.route);
    this.route.params.subscribe(params =>{
      if(!isNullOrUndefined(params['name'])){
        this.monitorService.getOneMonitorEntry(params['name']).subscribe(entry => {
          this.monitorEntry = entry;
          this.setJobDescriptor(entry);
        });
      } else{
        this.monitorEntry = new MonitorEntry();
      }
    });
  }

  public submit():void{
    if(this.isInMode(ComponentMode.CREATE)){
      this.setJobDescriptor(this.monitorEntry);
      console.log(this.monitorEntry.serviceName);
      this.monitorService.createMonitorEntry(this.monitorEntry).subscribe(val => {});
      console.log(this.jobDescriptor.serviceName)
      this.scheduleService.createJob(this.jobDescriptor).subscribe(val => this.router.navigate(["/admin/monitor"]));
    } else if(this.isInMode(ComponentMode.EDIT)){
      this.monitorService.updateMonitorEntry(this.monitorEntry).subscribe(val =>{
        if(this.monitorEntry.checkInterval !== this.jobDescriptor.checkInterval){
          this.scheduleService.updateJob(this.jobDescriptor).subscribe();
        }
        this.router.navigate(["/admin/monitor"]);
      });
    }
  }

  private setJobDescriptor(entry: MonitorEntry):void{
    this.jobDescriptor.serviceName = entry.serviceName;
    this.jobDescriptor.checkInterval = entry.checkInterval;
  }

}
