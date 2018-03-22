import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { NavbarComponent } from '../shared/index';
import { FooterComponent } from '../shared/index';

@Component({
  selector: 'app-changelog',
  templateUrl: './changelog.component.html',
  styleUrls: ['./changelog.component.css']
})
export class ChangelogComponent implements OnInit {

  constructor(private router: Router) { }

  ngOnInit() {

  }

  public isRouteLogin(): boolean {
  	return this.router.url === '/login'? true : false;
  }

}
