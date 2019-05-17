import {Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {interval, Subscription} from "rxjs";
import {AuthService} from "../../auth/auth.service";
import {DomainService} from "../../service";
import {InternationalizationService} from "../../service/internationalization.service";
import {MonitorService} from "../../service/monitor.service";
import {forEach} from "@angular/router/src/utils/collection";
import {MonitorEntry} from "../../model/monitorentry";
import {ContentDisplayService} from "../../service/content-display.service";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnChanges {

    public languages: string[];
    public refresh: Subscription;
    public isServiceAvailable: boolean;

    constructor(private router: Router, public authService: AuthService, private translate: TranslateService,
                private languageService:InternationalizationService, private domainService: DomainService) {
    }

    useLanguage(language: string) {
        this.translate.use(language);
    }

    getCurrent(){
        return this.translate.currentLang;
    }

    getPathToCurrent(){
        return "assets/images/country/" + this.getCurrent() + "_circle.png";
    }

    ngOnInit() {
        this.getSupportedLanguages();
        if(this.authService.isLogged()) {
            if (this.authService.hasRole('ROLE_SYSTEM_ADMIN')) {
                this.refresh = interval(5000).subscribe(next => {
                    if (this.languageService.shouldUpdate()) {
                        this.getSupportedLanguages();
                        this.languageService.setUpdateRequiredFlag(false);
                    }
                });
            }
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
    }

    public getSupportedLanguages(){
        this.languageService.getEnabledLanguages().subscribe(langs =>{
            this.translate.addLangs(langs);
            this.languages = langs;
        });
    }

    public checkUserRole(): boolean {
        return this.authService.getDomains().filter(value => value != this.domainService.getGlobalDomainId()).length > 0
          || this.authService.getRoles().filter(value => value != 'ROLE_INCOMPLETE')
            .filter(value => value != 'ROLE_GUEST')
            .length > 0;
    }

}
