import { Component, OnInit, ViewEncapsulation, Input } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import { Application } from '../../../model/application';
import { AppConfigService } from '../../../service/appconfig.service';
import { AppImagesService } from '../../../service/appimages.service';
import { RateComponent } from '../../../shared/rate/rate.component';
import { DefaultLogo } from '../../../directive/defaultlogo.directive';


@Component({
  selector: 'nmaas-applist-element',
  providers: [ DefaultLogo, RateComponent, AppImagesService ],
  templateUrl: './appelement.component.html',
  styleUrls: [ './appelement.component.css' ],
  encapsulation: ViewEncapsulation.None
})
export class AppElementComponent implements OnInit {
        
    @Input() 
    app:Application;
    
    private logoUrl:string;
    
    constructor(private appConfig:AppConfigService, private appImagesService: AppImagesService) { }

    ngOnInit() {
        this.logoUrl = this.appConfig.getApiUrl() + '/apps/' + this.app.id + '/logo';
    }
}
