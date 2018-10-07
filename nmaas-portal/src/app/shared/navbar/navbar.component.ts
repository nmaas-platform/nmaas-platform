import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  constructor(private translate: TranslateService) { 
    const browserLang = translate.getBrowserLang();
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }

  ngOnInit() {
  }

}
