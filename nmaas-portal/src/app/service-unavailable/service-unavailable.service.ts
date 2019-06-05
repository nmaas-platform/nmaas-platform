import { Injectable } from '@angular/core';
import {MonitorService} from "../service/monitor.service";

@Injectable({
  providedIn: 'root'
})
export class ServiceUnavailableService {

  public isServiceAvailable: boolean;

  constructor(private monitorService: MonitorService) {
    this.isServiceAvailable = false;
  }

  async validateServicesAvailability() {
    this.isServiceAvailable = true;
    try {
      let services = await Promise.resolve(this.monitorService.getAllMonitorEntries().toPromise())
        .catch(err => {
          console.debug("Error;");
          this.isServiceAvailable = false;
        });
      if (services) {
        services.forEach(value => {
          if (value.serviceName.toString() == "DATABASE") {
            if (value.status.toString() == "FAILURE") {
              console.debug("Database error;");
              this.isServiceAvailable = false;
            }
          }
        });
      } else {
        console.debug("No services;");
        this.isServiceAvailable = false;
      }
    } catch (err) {
      console.debug("Error #2;");
      this.isServiceAvailable = false;
    }
  }



}
