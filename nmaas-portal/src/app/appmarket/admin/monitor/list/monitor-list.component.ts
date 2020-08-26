import {Component, OnInit} from '@angular/core';
import {MonitorService} from '../../../../service/monitor.service';
import {MonitorEntry, ServiceType, TimeFormat} from '../../../../model/monitorentry';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-montiorlist',
  templateUrl: './monitor-list.component.html',
  styleUrls: ['./monitor-list.component.css']
})
export class MonitorListComponent implements OnInit {

  public monitorEntries: MonitorEntry[] = [];

  public services: typeof ServiceType = ServiceType;

  constructor(private monitorService: MonitorService, private translate: TranslateService) {
  }

  ngOnInit() {
    this.update();
  }

  private update() {
      this.monitorService.getAllMonitorEntries().subscribe(entries => this.monitorEntries = entries);
  }

  public executeJob(serviceName: string) {
    this.monitorService.executeJob(serviceName).subscribe(val => this.update());
  }

  public changeJobState(monitorEntry: MonitorEntry) {
    if (monitorEntry.active) {
      this.monitorService.pauseJob(monitorEntry.serviceName).subscribe(() => monitorEntry.active = false);
    } else {
      this.monitorService.resumeJob(monitorEntry.serviceName).subscribe(() => monitorEntry.active = true);
    }
  }

  public getCorrectStateLabel(active: boolean): string {
    if (active) {
      return this.translate.instant('MONITOR.DEACTIVATE_BUTTON');
    }
    return this.translate.instant('MONITOR.ACTIVATE_BUTTON');
  }

  public getIntervalCheck(checkInterval: number, timeFormat: TimeFormat): string {
    if (checkInterval === 1 && timeFormat.toString() === TimeFormat[TimeFormat.MIN]) {
      return '1 minute';
    } else if (checkInterval === 1 && timeFormat.toString() === TimeFormat[TimeFormat.H]) {
      return '1 hour';
    }
    return timeFormat.toString() === TimeFormat[TimeFormat.MIN] ? checkInterval + ' minutes' : checkInterval + ' hours';
  }

}
