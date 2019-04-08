import { Component, ViewEncapsulation } from '@angular/core';
import { AppConfigService } from './service/appconfig.service';
import {TranslateService} from "@ngx-translate/core";
import {ConfigurationService} from "./service";
import {AuthService} from "./auth/auth.service";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'nmaas-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ],
  encapsulation: ViewEncapsulation.None
})
export class AppComponent {
    config: any;
    
    constructor(private appConfigService: AppConfigService, private configService: ConfigurationService, private authService: AuthService, private translate: TranslateService) {
    }
    
    ngOnInit() {
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
                this.translate.setDefaultLang("en");
            });
        }
    }

    private setLanguage(lang: string){
        this.translate.use(lang);
        this.translate.setDefaultLang(lang);
    }
}
