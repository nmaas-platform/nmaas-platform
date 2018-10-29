import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../auth/auth.service';
import {DomainService} from "../../service";

import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'nmaas-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  providers: [ AuthService, TranslateService]
})
export class NavbarComponent implements OnInit {

    public languageActual = '';
    public pathToIcon = '';

  constructor(public authService: AuthService, public domainService: DomainService, private translate: TranslateService) {
    translate.addLangs(['en', 'fr', 'pl']);
    const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
    this.languageActual = this.getCurrent();
    this.pathToIcon = 'assets/images/country/' + this.getCurrent() + '_circle.png';
  }

  ngOnInit() {
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

  public checkUserRole(): boolean {
    return this.authService.getDomains().filter(value => value ! = this.domainService.getGlobalDomainId()).length > 0
        || this.authService.getRoles().filter(value => value ! = 'ROLE_GUEST').length > 0;
  }
}
