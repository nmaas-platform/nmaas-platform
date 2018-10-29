import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { AppConfigService } from './service/appconfig.service';
import {TranslateService} from "@ngx-translate/core";
import {ConfigurationService} from "./service";

@Component({
  selector: 'nmaas-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ],
  encapsulation: ViewEncapsulation.None
})
export class AppComponent {
    config: any;
    
    constructor(private appConfigService: AppConfigService, private configService: ConfigurationService, private translate: TranslateService) {
    }
    
    ngOnInit() {
        this.translate.use("en");
        this.configService.getConfiguration().subscribe(config => {
            this.translate.use(config.defaultLanguage)
            this.translate.setDefaultLang(config.defaultLanguage);
        },error1 => {
            this.translate.setDefaultLang("en");
        });
        this.config = this.appConfigService.config;
        console.log('Configuration: ' + JSON.stringify(this.config));
    }
}
