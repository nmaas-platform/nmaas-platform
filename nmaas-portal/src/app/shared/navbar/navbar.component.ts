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
    const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }
  useLanguage(language: string) {
    this.translate.use(language);
    this.translate.setDefaultLang(language);
  }
  ngOnInit() {
  }
}
