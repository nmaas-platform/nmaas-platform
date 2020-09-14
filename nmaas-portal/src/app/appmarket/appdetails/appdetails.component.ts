// angular
import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {isNullOrUndefined, isUndefined} from 'util';
import {TranslateService} from '@ngx-translate/core';
// pipes/components
import {RateComponent} from '../../shared/rate';
import {CommentsComponent} from '../../shared/comments';
import {AppInstallModalComponent} from '../../shared/modal/appinstall';
// services
import {AppConfigService, AppImagesService, AppsService, DomainService} from '../../service';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {AuthService} from '../../auth/auth.service';
// model
import {AppSubscription} from '../../model';
import {Role} from '../../model/userrole';
import {ApplicationState} from '../../model/application-state';
import {Domain} from '../../model/domain';
import {AppDescription} from '../../model/app-description';
import {ApplicationBase} from '../../model/application-base';


@Component({
    selector: 'nmaas-appdetails',
    templateUrl: './appdetails.component.html',
    styleUrls: ['../../../assets/css/main.css', './appdetails.component.css'],
    providers: []
})
export class AppDetailsComponent implements OnInit {

    public defaultTooltipOptions = {
        'display': true,
        'placement': 'bottom',
        'show-delay': '50',
        'theme': 'dark'
    };

    public linksTooltipOptions = {
        'placement': 'bottom',
        'show-delay': '50',
        'theme': 'dark'
    };

    protected state = 0;

    @ViewChild(AppInstallModalComponent)
    public readonly appInstallModal: AppInstallModalComponent;

    @ViewChild(CommentsComponent, { static: true })
    public readonly comments: CommentsComponent;

    @ViewChild(RateComponent)
    public readonly appRate: RateComponent;

    public appId: number;
    public app: ApplicationBase;
    public subscribed: boolean;
    public domainId: number;
    public active = false;
    public versionVisible = false;
    public domain: Domain;

    constructor(private appsService: AppsService,
                private appSubsService: AppSubscriptionsService,
                public appImagesService: AppImagesService,
                private userDataService: UserDataService,
                private appConfig: AppConfigService,
                private authService: AuthService,
                private translate: TranslateService,
                private domainService: DomainService,
                private router: Router,
                private route: ActivatedRoute) {
    }

    ngOnInit() {
        this.route.params.subscribe(params => {
            this.appId = +params['id'];
            this.appsService.getApplicationBase(this.appId).subscribe(
                application => {
                    this.app = application;
                    this.active = application.versions.some(version => this.getStateAsString(version.state) === 'ACTIVE');
                    // required for the tooltip to appear correctly
                    this.userDataService.selectedDomainId.subscribe((domainId) => this.updateDomainSelection(domainId));
                },
                err => {
                    console.error(err);
                    if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
                        this.router.navigateByUrl('/notfound');
                    }
                });
        });
    }

    public onRateChanged(): void {
        this.appRate.refresh();
    }

    protected updateDomainSelection(domainId: number): void {
        console.log('selected domainId:' + domainId);
        this.domainId = domainId;

        if (isUndefined(this.appId)) {
            return;
        }

        let result: Observable<any>;
        if (isUndefined(domainId) || domainId === 0 || this.appConfig.getNmaasGlobalDomainId() === domainId) {
            result = this.appSubsService.getAllByApplication(this.appId);
            result.subscribe(() => this.subscribed = false);

            this.domain = undefined;
        } else {
            result = this.appSubsService.getSubscription(this.appId, domainId);
            result.subscribe((appSub: AppSubscription) => this.subscribed = appSub.active, error => this.subscribed = false);

            this.domainService.getOne(this.domainId).subscribe(d => {
                this.domain = d;
                this.defaultTooltipOptions.display = !this.isApplicationEnabledInDomain();
            });
        }
    }

    public subscribe(): void {
        if (this.isSubscriptionAllowed()) {
            console.info('Subscribe appId=' + this.appId + ' to domainId=' + this.domainId);
            this.appSubsService.subscribe(this.domainId, this.appId).subscribe(() => this.subscribed = true);
        }
    }

    public unsubscribe(): void {
        if (this.isSubscriptionAllowed()) {
            console.info('Unsubscribe appId=' + this.appId + ' from domainId=' + this.domainId);
            this.appSubsService.unsubscribe(this.domainId, this.appId).subscribe(() => this.subscribed = false);
        }
    }

    public isSubscriptionAllowed(): boolean {
        if (isUndefined(this.domainId) || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
            return false;
        }

        return this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN])
            || this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_DOMAIN_ADMIN]);
    }

    public isDeploymentAllowed(): boolean {
        if (isUndefined(this.domainId) || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
            return false;
        }

        return this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN])
            || this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_DOMAIN_ADMIN]);
    }

    public isApplicationEnabledInDomain(): boolean {
        if (!this.domain || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
            return false;
        }
        const appStatus = this.domain.applicationStatePerDomain.find(value => value.applicationBaseId === this.app.id);
        if (!appStatus) {
            return false;
        }
        return appStatus.enabled;
    }

    protected refresh(): void {
        this.state += Math.random() * 123456;
    }

    public getDescription(): AppDescription {
        if (isNullOrUndefined(this.app)) {
            return;
        }
        return this.app.descriptions.find(val => val.language === this.translate.currentLang);
    }

    public getPathUrl(id: number): string {
        if (!isNullOrUndefined(id) && !isNaN(id)) {
            return '/apps/' + id + '/rate/my';
        } else {
            return '';
        }
    }

    public getStateAsString(state: any): string {
        return typeof state === 'string' && isNaN(Number(state.toString())) ? state : ApplicationState[state];
    }

    public getValidLink(url: string): string {
        if (isNullOrUndefined(url)) {
            return;
        }
        if (!url.startsWith('http://') && !url.startsWith('https://')) {
            return '//' + url;
        }
        return url;
    }

    public showVersions() {
        this.versionVisible = !this.versionVisible;
    }

}
