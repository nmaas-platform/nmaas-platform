/* tslint:disable:no-unused-variable */

import {TestBed, async, ComponentFixture} from '@angular/core/testing';
import {AppDetailsComponent} from './appdetails.component';
import {RouterTestingModule} from '@angular/router/testing';
import {AppConfigService, AppImagesService, AppInstanceService, AppsService, DomainService} from '../../service';
import {Component, Input, Pipe, PipeTransform} from '@angular/core';
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {AuthService} from '../../auth/auth.service';
import {TooltipModule} from 'ng2-tooltip-directive';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {ApplicationBase} from '../../model/application-base';

@Pipe({
    name: 'secure'
})
class SecurePipeMock implements PipeTransform {
    public name = 'secure';

    public transform(query: string, ...args: any[]): any {
        return query;
    }
}

@Component({
    selector: 'rate',
    template: '<p>Rate Mock</p>'
})
class MockRateComponent {
    @Input()
    private pathUrl: string;
    @Input()
    editable = false;
    @Input()
    short = false;
    @Input()
    showVotes = false;
}

@Component({
    selector: 'rating-extended',
    template: '<p>Rate Extended mock</p>'
})
class MockRateExtendedComponent {
    @Input()
    private pathUrl: string;
    @Input()
    editable = false;
    @Input()
    short = false;
    @Input()
    showVotes = false;
}

@Component({
    selector: 'comments',
    template: '<p>Mock comments component</p>'
})
class MockCommentsComponent {
    @Input()
    pathUrl: string;
}

@Component({
    selector: 'nmaas-modal-app-install',
    template: '<p>Nmaas modal app install mock</p>'
})
class MockNmassModalAppInstallComponent {
    @Input()
    app: any;

    @Input()
    domain: any;
}

@Component({
    selector: 'screenshots',
    template: '<p>Screenchots Mock</p>'
})
class MockScreenshotsComponent {
    @Input()
    pathUrl: string;
}

describe('Component: AppDetails', () => {
    let component: AppDetailsComponent;
    let fixture: ComponentFixture<AppDetailsComponent>;

    const application: ApplicationBase = {
        id: 2,
        name: 'Oxidized',
        license: null,
        licenseUrl: null,
        wwwUrl: null,
        sourceUrl: null,
        issuesUrl: null,
        nmaasDocumentationUrl: null,
        descriptions: [],
        tags: [
            {id: null, name: 'tag1'},
            {id: null, name: 'tag2'}
        ],
        versions: [],
        rate: null,
    };

    beforeEach(async(() => {
        const appsServiceSpy = jasmine.createSpyObj('AppsService', ['getApplicationBase']);
        appsServiceSpy.getApplicationBase.and.returnValue(of(application));
        const appSubsServiceSpy = jasmine.createSpyObj('AppSubscriptionService', ['getAllByApplication', 'getSubscription', 'unsubscribe']);
        appSubsServiceSpy.getAllByApplication.and.returnValue(of([]));
        const appImagesServiceSpy = jasmine.createSpyObj('AppImagesService', ['getAppLogoUrl']);
        const appConfigSpy = jasmine.createSpyObj('AppConfigService', ['getNmaasGlobalDomainId', 'getApiUrl', 'getHttpTimeout']);
        appConfigSpy.getApiUrl.and.returnValue('http://localhost:9000/');
        appConfigSpy.getNmaasGlobalDomainId.and.returnValue(1);
        appConfigSpy.getHttpTimeout.and.returnValue(10000);
        const authServiceSpy = jasmine.createSpyObj('AuthService', ['hasRole', 'hasDomainRole']);
        const domainServiceSpy = jasmine.createSpyObj('DomainService', ['getOne']);

        TestBed.configureTestingModule({
            declarations: [
                AppDetailsComponent,
                SecurePipeMock,
                MockRateComponent,
                MockCommentsComponent,
                MockNmassModalAppInstallComponent,
                MockRateExtendedComponent,
                MockScreenshotsComponent
            ],
            imports: [
                RouterTestingModule,
                TooltipModule,
                HttpClientTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: AppConfigService, useValue: appConfigSpy},
                UserDataService,
                {provide: AppsService, useValue: appsServiceSpy},
                {provide: AppSubscriptionsService, useValue: appSubsServiceSpy},
                {provide: AppImagesService, useValue: appImagesServiceSpy},
                // {provide: AppInstanceService, useValue: appInstanceServiceSpy},
                {provide: AuthService, useValue: authServiceSpy},
                {provide: DomainService, useValue: domainServiceSpy},
                {provide: ActivatedRoute, useValue: {params: of({id: 1})}}
            ],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppDetailsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create app', () => {
        expect(component).toBeDefined();
        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    })

});
