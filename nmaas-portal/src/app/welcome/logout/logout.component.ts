import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import {ConfigurationService} from '../../service';
import {SSOService} from '../../service/sso.service';


@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit {

  constructor(private router: Router,
              private auth: AuthService,
              private configService: ConfigurationService,
              private ssoService: SSOService) { }

  ngOnInit() {
      this.auth.logout();
      this.configService.getConfiguration().subscribe(config => {
          if (config.ssoLoginAllowed && this.auth.loginUsingSsoService) {
              const url = window.location.origin;
              this.ssoService.getOne().subscribe(sso => {
                  // Shibboleth SP uses parameter 'target' instead of 'return'
                  window.location.href = sso.logoutUrl + '?return=' + url;
              });
          } else {
              this.router.navigate(['/welcome']);
          }
      });
  }

}
