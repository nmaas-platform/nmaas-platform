import {Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {interval, Observable, Subscription} from "rxjs";
import {ContentDisplayService} from "../../service/content-display.service";
import {AuthService} from "../../auth/auth.service";
import {DomainService} from "../../service";
import {MonitorService} from "../../service/monitor.service";
import {forEach} from "@angular/router/src/utils/collection";
import {MonitorEntry} from "../../model/monitorentry";

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
                private contentService: ContentDisplayService, private domainService: DomainService,
                private monitorService: MonitorService) {
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
                    if (this.contentService.shouldUpdate()) {
                        this.getSupportedLanguages();
                        this.contentService.setUpdateRequiredFlag(false);
                    }
                });
            }
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
    }

    public getSupportedLanguages(){
        this.contentService.getLanguages().subscribe(langs =>{
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
