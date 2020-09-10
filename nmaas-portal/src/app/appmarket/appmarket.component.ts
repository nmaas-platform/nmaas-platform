import {AfterContentChecked, AfterViewChecked, Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {ServiceUnavailableService} from '../service-unavailable/service-unavailable.service';
import {Router} from '@angular/router';
import {AppConfigService, ConfigurationService} from '../service';
import {ModalTestInstanceComponent} from '../shared/modal/modal-test-instance/modal-test-instance.component';
import {ModalGuestUserComponent} from './modals/modal-guest-user/modal-guest-user.component';
import {AuthService} from '../auth/auth.service';
import {Role} from '../model/userrole';
import {ModalProvideSshKeyComponent} from '../shared/modal/modal-provide-ssh-key/modal-provide-ssh-key.component';
import {SSHKeyService} from '../service/sshkey.service';

@Component({
  selector: 'app-appmarket',
  templateUrl: './appmarket.component.html',
  styleUrls: [ '../../assets/css/main.css', './appmarket.component.css'],
  providers: [ModalTestInstanceComponent]
})
export class AppMarketComponent implements OnInit, AfterViewChecked, AfterContentChecked {

  private height = 0;
  private navHeight = 0;
  @ViewChild(ModalTestInstanceComponent, { static: true })
  private testInstanceModal: ModalTestInstanceComponent;

  @ViewChild(ModalGuestUserComponent, { static: true })
  private guestUserModal: ModalGuestUserComponent;

  @ViewChild(ModalProvideSshKeyComponent, { static: true })
  private provideSshKeyModal: ModalProvideSshKeyComponent;

  constructor(private router: Router,
              private serviceHealth: ServiceUnavailableService,
              private configService: ConfigurationService,
              private appConfig: AppConfigService,
              private authService: AuthService,
              private sshKeyService: SSHKeyService) { }

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
      );
      const domainRoles = this.authService.getDomainRoles();
      const globalRoles = domainRoles.get(this.appConfig.getNmaasGlobalDomainId());
      if (domainRoles.size === 1 && globalRoles && globalRoles.getRoles().length === 1 && globalRoles.hasRole(Role[Role.ROLE_GUEST])) {
          this.guestUserModal.modal.show();
      }


      if (this.authService.hasRole('ROLE_DOMAIN_ADMIN') && this.sshKeyService.getAll()) {
          this.sshKeyService.getAll().subscribe(
              keys => {
                  if (keys.length === 0) {
                      this.provideSshKeyModal.modal.show();
                  }
              }
          )
      }
  }

  ngAfterViewChecked() {

  }

  ngAfterContentChecked() {

  }

}
