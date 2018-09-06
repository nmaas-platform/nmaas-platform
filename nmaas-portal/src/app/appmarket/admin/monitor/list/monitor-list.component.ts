import { Component, OnInit } from '@angular/core';
import {ScheduleService} from "../../../../service/schedule.service";
import {MonitorService} from "../../../../service/monitor.service";
import {Router} from "@angular/router";
import {MonitorEntry} from "../../../../model/monitorentry";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'app-montiorlist',
  templateUrl: './monitor-list.component.html',
  styleUrls: ['./monitor-list.component.css']
})
export class MonitorListComponent implements OnInit {

  private monitorEntries: MonitorEntry[] = [];

  constructor(private scheduleService: ScheduleService, private monitorService: MonitorService, private router: Router) {}

  ngOnInit() {
    this.update();
  }

  private update(){
      this.monitorService.getAllMonitorEntries().subscribe(entries => this.monitorEntries = entries);
  }

  public executeJob(serviceName: string){
    this.scheduleService.executeJob(serviceName).subscribe(val => this.update());
  }

  public removeJob(serviceName: string){
    this.scheduleService.deleteJob(serviceName);
    this.monitorService.deleteMonitorEntry(serviceName).subscribe(val => this.update());
  }

  public getIntervalCheck(checkInterval: number):string{
    if(checkInterval === 1){
      return checkInterval + " minute";
    } else if(checkInterval === 60){
      return "1 hour";
    }
    return (checkInterval%60) === 0?(checkInterval/60)+ " hours":checkInterval + " minutes";
  }

}
