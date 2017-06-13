import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { AppConfigService } from './service/appconfig.service';

@Component({
  selector: 'nmaas-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ],
  encapsulation: ViewEncapsulation.None
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
