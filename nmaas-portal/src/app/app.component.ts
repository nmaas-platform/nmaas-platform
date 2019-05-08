import { Component, ViewEncapsulation } from '@angular/core';
import { AppConfigService } from './service/appconfig.service';
import {TranslateService} from "@ngx-translate/core";
import {ConfigurationService} from "./service";
import {AuthService} from "./auth/auth.service";
import {isNullOrUndefined} from "util";
import {MonitorService} from "./service/monitor.service";
import {Router} from "@angular/router";

@Component({
  selector: 'nmaas-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ],
  encapsulation: ViewEncapsulation.None
})
export class AppComponent {
    config: any;
    isServiceAvailable: boolean;
    
    constructor(private appConfigService: AppConfigService, private configService: ConfigurationService,
                private authService: AuthService, private translate: TranslateService,
                private monitorService: MonitorService, private router: Router) {
    }

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
                            console.debug("#2");
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

    async ngOnInit() {
        await this.validateServicesAvailability();
        if(!this.isServiceAvailable){
            this.router.navigate(['/service-unavailable']);
        }
        this.handleDefaultLanguage();
        this.config = this.appConfigService.config;
        console.log('Configuration: ' + JSON.stringify(this.config));
    }

    public handleDefaultLanguage() : void {
        if(!isNullOrUndefined(this.authService.getSelectedLanguage())){
            this.setLanguage(this.authService.getSelectedLanguage());
        } else {
            this.configService.getConfiguration().subscribe(config => {
                this.setLanguage(config.defaultLanguage);
            },() => {
                this.setLanguage("en");
            });
        }
    }

    private setLanguage(lang: string){
        this.translate.use(lang);
        this.translate.setDefaultLang(lang);
    }
}
