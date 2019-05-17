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
      this.onResize();
  }

  ngAfterViewChecked(){
      this.onResize();
  }

  ngAfterContentChecked(){
      this.onResize();
  }

    onResize() {
        this.height = document.getElementById("global-footer").offsetHeight;
        this.navHeight = document.getElementById("navbar").offsetHeight;
        document.getElementById("appmarket-container").style.marginBottom = `${this.height}px`;
        if(this.height > 90){
            document.getElementById("global-footer").style.textAlign = "center";
        }else{
            document.getElementById("appmarket-container").style.paddingTop = `${this.navHeight + 10}px`;
            document.getElementById("global-footer").style.textAlign = "right";
        }
    }
}
