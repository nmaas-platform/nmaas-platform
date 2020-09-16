import {Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {interval, Subscription} from 'rxjs';
import {AuthService} from '../../auth/auth.service';
import {DomainService} from '../../service';
import {InternationalizationService} from '../../service/internationalization.service';
import {ModalNotificationSendComponent} from '../modal/modal-notification-send/modal-notification-send.component';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnChanges {

    @ViewChild(ModalNotificationSendComponent, { static: true })
    public notificationModal;

    public languages: string[];
    public refresh: Subscription;
    public isServiceAvailable: boolean;

    constructor(public router: Router,
                public authService: AuthService,
                private translate: TranslateService,
                private languageService: InternationalizationService,
                private domainService: DomainService) {
    }

    useLanguage(language: string) {
        this.translate.use(language);
    }

    getCurrent() {
        return this.translate.currentLang;
    }

    getPathToCurrent() {
        return 'assets/images/country/' + this.getCurrent() + '_circle.png';
    }

    ngOnInit() {
        this.getSupportedLanguages();
        if (this.authService.isLogged()) {
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

    public getSupportedLanguages() {
        this.languageService.getEnabledLanguages().subscribe(langs => {
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

    public showNotificationModal(): void {
        this.notificationModal.show();
    }

    public isOnlyGuestInGlobalDomain(): boolean {
        const globalDomainRoles = this.authService.getDomainRoles().get(this.domainService.getGlobalDomainId()).getRoles()
        return globalDomainRoles  // does have any role in global domain (not undefined)
            && globalDomainRoles.length === 1  // only one role in global domain
            && globalDomainRoles[0] === 'ROLE_GUEST'  // this single role is ROLE_GUEST
            && this.authService.getDomains().length === 1 // no roles in other domains
    }

}
