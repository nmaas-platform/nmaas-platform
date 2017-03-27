import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Params }   from '@angular/router';
import { Location }                 from '@angular/common';
//import 'rxjs/add/operator/switchMap';


import { AppsService } from '../../service/apps.service';
import { Application } from '../../model/application';

@Component({
  selector: 'app-appinstall',
  templateUrl: './appinstall.component.html',
  styleUrls: [ '../../../assets/css/stepwizard.css', '../appdetails/appdetails.component.css'],
    providers: [ AppsService ]
})
export class AppInstallComponent implements OnInit {

   app: Application;    
    
    private id: Number;    
    
    constructor(private appsService: AppsService, private route: ActivatedRoute, private location: Location) { }

    ngOnInit() {
        this.route.params.subscribe(params => { 
            this.id = +params['id'];
            this.appsService.getApp(this.id).subscribe(application => this.app = application);
        });
    }

}
