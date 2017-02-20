import { Component, OnInit } from '@angular/core';
import { AppConfigService } from './service/appconfig.service';

@Component({
  selector: 'nmaas-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ]
})
export class AppComponent {
    config: any;
    
    constructor(private appConfigService: AppConfigService) {
    }
    
    ngOnInit() {
        this.config = this.appConfigService.config;
        console.log('Configuration: ' + JSON.stringify(this.config));
    }
}
