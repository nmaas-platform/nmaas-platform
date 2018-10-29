import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../auth/auth.service';
import {DomainService} from "../../service";

import {TranslateService} from '@ngx-translate/core';
import {ContentDisplayService} from "../../service/content-display.service";
import {Observable, Subscription} from "rxjs";

@Component({
  selector: 'nmaas-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  providers: [ AuthService, TranslateService]
})
export class NavbarComponent implements OnInit {

  public languages: string[];

  public refresh: Subscription;

  constructor(public authService: AuthService, public domainService: DomainService, private translate: TranslateService, private contentService:ContentDisplayService) {
  }

  useLanguage(language: string) {
    this.translate.use(language);
  }

  ngOnInit() {
      if(this.authService.hasRole('ROLE_SYSTEM_ADMIN')){
          this.refresh = Observable.interval(5000).subscribe(next => {
              if(this.contentService.shouldUpdate()) {
                  this.getSupportedLanguages();
                  this.contentService.setUpdateRequiredFlag(false);
              }
          });
      }
      this.getSupportedLanguages()
  }

  public checkUserRole(): boolean {
    return this.authService.getDomains().filter(value => value ! = this.domainService.getGlobalDomainId()).length > 0
        || this.authService.getRoles().filter(value => value ! = 'ROLE_GUEST').length > 0;
  }

  public getSupportedLanguages(){
    this.contentService.getLanguages().subscribe(langs =>{
        this.translate.addLangs(langs);
        this.languages = langs;
    });
  }
}
