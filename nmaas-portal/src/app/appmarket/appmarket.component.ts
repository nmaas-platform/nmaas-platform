import {AfterContentChecked, AfterViewChecked, Component, OnInit, ViewEncapsulation} from '@angular/core';
import {ServiceUnavailableService} from "../service-unavailable/service-unavailable.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-appmarket',
  templateUrl: './appmarket.component.html',
  styleUrls: [ '../../assets/css/main.css', './appmarket.component.css' ]
})
export class AppMarketComponent implements OnInit, AfterViewChecked, AfterContentChecked {

  private height = 0;
  private navHeight = 0;

  constructor(private router: Router, private serviceHealth: ServiceUnavailableService) { }

  async ngOnInit() {
      await this.serviceHealth.validateServicesAvailability();
      if(!this.serviceHealth.isServiceAvailable){
        this.router.navigate(['/service-unavailable']);
      }

  }

  ngAfterViewChecked(){

  }

  ngAfterContentChecked(){

  }

}
