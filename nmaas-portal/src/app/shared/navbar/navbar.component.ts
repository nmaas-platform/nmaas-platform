import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  public languageActual = '';
  public pathToIcon = '';

  constructor(private translate: TranslateService) {
    const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
    this.languageActual = this.getCurrent();
    this.pathToIcon = 'assets/images/country/' + this.getCurrent() + '_circle.png';
  }

  useLanguage(language: string) {
    this.translate.use(language);
    this.translate.setDefaultLang(language);
    this.languageActual = this.getCurrent();
    this.pathToIcon = 'assets/images/country/' + language + '_circle.png';
  }

  getCurrent(){
    return this.translate.currentLang;
  }

  ngOnInit() {
  }
}
