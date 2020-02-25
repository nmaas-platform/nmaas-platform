import {Component, OnInit} from '@angular/core';
import {MonitorService} from "../../../../service/monitor.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MonitorEntry, ServiceType, TimeFormat} from "../../../../model/monitorentry";
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
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

    public monitorEntry: MonitorEntry;

    private errMsg: string;

    constructor(private monitorService: MonitorService, private router: Router, private route: ActivatedRoute) {
        super();
    }

    ngOnInit() {
        this.mode = this.getMode(this.route);
        this.route.params.subscribe(params => {
            if (!isNullOrUndefined(params['name'])) {
                this.monitorService.getOneMonitorEntry(params['name']).subscribe(
                    entry => {
                        this.monitorEntry = entry;
                        if (this.getTimeFormatAsString(entry.timeFormat) === 'H') {
                            this.monitorEntry.timeFormat = TimeFormat.H;
                        } else {
                            this.monitorEntry.timeFormat = TimeFormat.MIN;
                        }
                    },
                    err => {
                        console.error(err);
                        if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403 || err.statusCode === 500)) {
                            this.router.navigateByUrl('/notfound');
                        }
                    });
            } else {
                this.monitorEntry = new MonitorEntry();
            }
        });
    }

    public getTimeFormatAsString(timeFormat: any): string {
        return typeof timeFormat === "string" && isNaN(Number(timeFormat.toString())) ? timeFormat : TimeFormat[timeFormat];
    }

    public submit(): void {
        this.monitorService.updateMonitorEntryAndJob(this.monitorEntry).subscribe(() => this.router.navigate(["/admin/monitor"]), err => this.errMsg = err.message);
    }

}
