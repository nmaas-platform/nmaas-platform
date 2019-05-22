import {Component, OnDestroy, OnInit} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {MonitorService} from "../service/monitor.service";
import {Router} from "@angular/router";
import {validate} from "codelyzer/walkerFactory/walkerFn";
import {interval} from "rxjs";

@Component({
  selector: 'app-service-unavailable',
  templateUrl: './service-unavailable.component.html',
  styleUrls: ['./service-unavailable.component.css']
})
export class ServiceUnavailableComponent implements OnInit, OnDestroy {
  private isServiceAvailable: boolean;
  private interval;

  constructor(private translateService: TranslateService, private monitorService: MonitorService,
              private router: Router) { }

  async validateServicesAvailability() {
    this.isServiceAvailable = true;
    try {
      let services = await Promise.resolve(this.monitorService.getAllMonitorEntries().toPromise())
        .catch(err => {
          console.debug(err);
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


  public changeLang(lang: string): void{
    console.debug("lang_change: ", lang);
    this.translateService.use(lang);
  }

  private async refresh(){
    console.debug('refresh');
    await this.validateServicesAvailability();
    if(this.isServiceAvailable == true) {
      this.router.navigate(['welcome']);
    }else{
        this.router.navigate(['service-unavailable']);
      }
  }

  async ngOnInit() {
    await this.validateServicesAvailability();
    if(this.isServiceAvailable == true){
      this.router.navigate(['welcome']);
  }
    this.interval = setInterval(() => {
      this.refresh();
    }, 10000);
  }

    ngOnDestroy() {
      if (this.interval){
        clearInterval(this.interval);
      }
    }

}
