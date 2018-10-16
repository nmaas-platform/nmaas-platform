import {Component, OnInit} from '@angular/core';
import {MonitorService} from "../../../../service/monitor.service";
import {Router} from "@angular/router";
import {MonitorEntry, ServiceType, TimeFormat} from "../../../../model/monitorentry";
import {Time} from "@angular/common";

@Component({
  selector: 'app-montiorlist',
  templateUrl: './monitor-list.component.html',
  styleUrls: ['./monitor-list.component.css']
})
export class MonitorListComponent implements OnInit {

  private monitorEntries: MonitorEntry[] = [];

  private services: typeof ServiceType = ServiceType;

  constructor(private monitorService: MonitorService, private router: Router) {}

  ngOnInit() {
    this.update();
  }

  private update(){
      this.monitorService.getAllMonitorEntries().subscribe(entries => this.monitorEntries = entries);
  }

  public executeJob(serviceName: string){
    this.monitorService.executeJob(serviceName).subscribe(val => this.update());
  }

  public removeJob(serviceName: string){
    this.monitorService.deleteMonitorEntryAndJob(serviceName).subscribe(val => this.update());
  }

  public getIntervalCheck(checkInterval:number, timeFormat:TimeFormat):string{
    if(checkInterval === 1 && timeFormat.toString() === TimeFormat[TimeFormat.MIN]){
      return "1 minute";
    } else if(checkInterval === 1 && timeFormat.toString() === TimeFormat[TimeFormat.H]){
      return "1 hour";
    }
    return timeFormat.toString() === TimeFormat[TimeFormat.MIN]? checkInterval + " minutes": checkInterval + " hours";
  }

}
