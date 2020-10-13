import { AppConfigService } from '../service/appconfig.service';
import {
    AfterContentChecked,
    AfterViewChecked,
    Component,
    OnInit,
} from '@angular/core';
import {Router} from '@angular/router';
import {ServiceUnavailableService} from '../service-unavailable/service-unavailable.service';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit, AfterViewChecked, AfterContentChecked {

  private height = 0;

  constructor(private appConfig: AppConfigService,
              public router: Router,
              private serviceHealth: ServiceUnavailableService) {
  }

  async ngOnInit() {
    document.getElementById('global-footer').style.display = 'block';
    await this.serviceHealth.validateServicesAvailability();
    if (!this.serviceHealth.isServiceAvailable) {
      this.router.navigate(['/service-unavailable']);
    }
    this.onResize();
  }

  ngAfterContentChecked() {
      this.onResize();
  }

  ngAfterViewChecked() {
      this.onResize();
  }

  onResize() {
      // TODO
      // rewrite this code, use css instead of js for better performance
      this.height = document.getElementById('global-footer').offsetHeight;
      const navHeight = document.getElementById('navbar').offsetHeight;
      // document.getElementById("welcome-container").style.marginBottom = `${this.height}px`;
      // document.getElementById("welcome-container").style.marginTop = `${navHeight + 2}px`;
      document.getElementById('login-out').style.maxHeight = `calc(95vh - ${this.height +  navHeight + 10}px)`;
      // document.getElementById('login-out').style.paddingTop = `${navHeight}`;
      if (this.height > 90) {
        document.getElementById('global-footer').style.textAlign = 'center';
        document.getElementById('login-out').style.maxHeight = `calc(94vh - ${this.height +  navHeight + 10}px)`;
      } else {
        document.getElementById('global-footer').style.textAlign = 'right';
      }
  }
}
