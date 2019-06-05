import { Component, ViewEncapsulation } from '@angular/core';
import { AppConfigService } from './service/appconfig.service';
import {TranslateService} from "@ngx-translate/core";
import {ConfigurationService} from "./service";
import {AuthService} from "./auth/auth.service";
import {isNullOrUndefined} from "util";
import {MonitorService} from "./service/monitor.service";
import {Router} from "@angular/router";
import {ServiceUnavailableService} from "./service-unavailable/service-unavailable.service";

@Component({
  selector: 'nmaas-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ],
  encapsulation: ViewEncapsulation.None
})
export class AppComponent {
    config: any;

    
    constructor(private appConfigService: AppConfigService, private configService: ConfigurationService,
                private authService: AuthService, private translate: TranslateService,
                private router: Router, private serviceHealth: ServiceUnavailableService) {
    }

    async ngOnInit() {
        if(this.serviceHealth.isServiceAvailable == false){
            this.router.navigate(['/service-unavailable']);
        }
        this.handleDefaultLanguage();
        this.config = this.appConfigService.config;
        console.debug('Configuration: ' + JSON.stringify(this.config));
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
