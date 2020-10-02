import {Component, OnDestroy, OnInit} from '@angular/core';
import {TranslateLoader, TranslateService} from '@ngx-translate/core';
import {MonitorService} from '../service/monitor.service';
import {Router} from '@angular/router';
import {InternationalizationService} from '../service/internationalization.service';
import {ServiceUnavailableService} from './service-unavailable.service';

@Component({
    selector: 'app-service-unavailable',
    templateUrl: './service-unavailable.component.html',
    styleUrls: ['./service-unavailable.component.css']
})
export class ServiceUnavailableComponent implements OnInit, OnDestroy {
    private interval;
    public languages: string[];

    constructor(private translateService: TranslateService,
                private monitorService: MonitorService,
                private router: Router,
                private languageService: InternationalizationService,
                private serviceAvailability: ServiceUnavailableService,
                private translateLoader: TranslateLoader) {
    }

    useLanguage(language: string) {
        this.translateService.use(language);
    }

    getCurrent() {
        return this.translateService.currentLang;
    }

    getPathToCurrent() {
        return 'assets/images/country/' + this.getCurrent() + '_circle.png';
    }

    private async refresh() {
        await this.serviceAvailability.validateServicesAvailability();
        if (this.serviceAvailability.isServiceAvailable) {
            this.translateLoader.getTranslation(this.getCurrent());
            document.getElementById('global-footer').style.display = 'block';
            this.router.navigate(['welcome']);
        } else {

            this.router.navigate(['service-unavailable']);
        }
    }

    async ngOnInit() {
        this.getSupportedLanguages();
        document.getElementById('global-footer').style.display = 'none';
        await this.serviceAvailability.validateServicesAvailability();
        if (this.serviceAvailability.isServiceAvailable) {
            this.translateLoader.getTranslation(this.getCurrent());
            document.getElementById('global-footer').style.display = 'block';
            this.router.navigate(['welcome']);
        }
        this.interval = setInterval(() => {
            this.refresh();
        }, 10000);
    }

    ngOnDestroy() {
        if (this.interval) {
            clearInterval(this.interval);
        }
    }

    public getSupportedLanguages() {
        this.languageService.getEnabledLanguages().subscribe(langs => {
            this.translateService.addLangs(langs);
            this.languages = langs;
        }, error => console.debug('oops', error));
        if (!this.languages) {
            this.languages = [];
            this.languages.push('en', 'de', 'fr', 'pl');
        }
    }
}
