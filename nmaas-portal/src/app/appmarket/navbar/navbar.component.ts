import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../auth/auth.service';
import {DomainService} from "../../service";

@Component({
  selector: 'nmaas-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  providers: [ AuthService ]
})
export class NavbarComponent implements OnInit {

  constructor(public authService: AuthService, public domainService:DomainService) {
  }

  ngOnInit() {
  }

  public checkUserRole():boolean{
    return this.authService.getDomains().filter(value => value != this.domainService.getGlobalDomainId()).length > 0
        || this.authService.getRoles().filter(value => value != 'ROLE_GUEST').length > 0;
  }
}
