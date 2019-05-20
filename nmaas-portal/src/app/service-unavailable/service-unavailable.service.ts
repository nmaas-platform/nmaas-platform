import { Injectable } from '@angular/core';
import {MonitorService} from "../service/monitor.service";

@Injectable({
  providedIn: 'root'
})
export class ServiceUnavailableService {

  public isServiceAvailable: boolean;

  constructor(private monitorService: MonitorService) { }

  async validateServicesAvailability() {
    this.isServiceAvailable = true;
    try {
      let services = await Promise.resolve(this.monitorService.getAllMonitorEntries().toPromise())
        .catch(err => {
          this.isServiceAvailable = false;
        });
      if (services) {
        services.forEach(value => {
          if (value.serviceName.toString() == "DATABASE") {
            if (value.status.toString() == "FAILURE") {
              this.isServiceAvailable = false;
            }
          }
        });
      } else {
        this.isServiceAvailable = false;
      }
    } catch (err) {
      this.isServiceAvailable = false;
    }
  }



}
