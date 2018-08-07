import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';


@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit {
      
  constructor(private router: Router, private auth: AuthService) { }

  ngOnInit() {
      this.auth.logout();
      if(this.auth.allowsSSO() && this.auth.loginUsingSsoService) {
          let url = window.location.origin;
          window.location.href.replace(/ssoUserId=.+/, '');
          // Shibboleth SP uses parameter 'target' instead of 'return'
          window.location.href = this.auth.getSSOLogoutUrl() + '?return=' + url;
      } else {
          this.router.navigate(['/welcome']);
      }
  }

}
