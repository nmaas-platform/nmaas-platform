import {AfterContentChecked, AfterViewChecked, Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {ServiceUnavailableService} from '../service-unavailable/service-unavailable.service';
import {Router} from '@angular/router';
import {AppConfigService, ConfigurationService} from '../service';
import {ModalTestInstanceComponent} from '../shared/modal/modal-test-instance/modal-test-instance.component';

@Component({
  selector: 'app-appmarket',
  templateUrl: './appmarket.component.html',
  styleUrls: [ '../../assets/css/main.css', './appmarket.component.css'],
  providers: [ModalTestInstanceComponent]
})
export class AppMarketComponent implements OnInit, AfterViewChecked, AfterContentChecked {

  private height = 0;
  private navHeight = 0;
  @ViewChild(ModalTestInstanceComponent)
  private testInstanceModal: ModalTestInstanceComponent;

  constructor(private router: Router, private serviceHealth: ServiceUnavailableService, private configService: ConfigurationService, private appConfig: AppConfigService) { }

  async ngOnInit() {
      await this.serviceHealth.validateServicesAvailability();
      if (!this.serviceHealth.isServiceAvailable) {
        this.router.navigate(['/service-unavailable']);
      }
      this.configService.getConfiguration().subscribe(
          config => {
            if (config.testInstance && localStorage.getItem(this.appConfig.getTestInstanceModalKey()) === 'True') {
              this.testInstanceModal.modal.show();
            }
            localStorage.setItem(this.appConfig.getTestInstanceModalKey(), 'False');
          }
      )
  }

  ngAfterViewChecked() {

  }

  ngAfterContentChecked() {

  }

}
