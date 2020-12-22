import {ComponentFixture, TestBed} from '@angular/core/testing';
import {AppInstanceComponent} from './appinstance.component';
import {FormsModule} from '@angular/forms';
import {JwtModule} from '@auth0/angular-jwt';
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {AppConfigService, AppImagesService, AppInstanceService, AppsService, DomainService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {of} from 'rxjs';
import {FormioModule} from 'angular-formio';
import {AppInstanceProgressComponent} from '../appinstanceprogress';
import {PipesModule} from '../../../pipe/pipes.module';
import {TooltipModule} from 'ng2-tooltip-directive';
import {NgxPaginationModule} from 'ngx-pagination';
import {AppRestartModalComponent} from '../modals/apprestart';
import {AppAbortModalComponent} from '../modals/app-abort-modal';
import {RouterTestingModule} from '@angular/router/testing';
import {StorageServiceModule} from 'ngx-webstorage-service';
import {AppInstanceState, User} from '../../../model';
import {Role} from '../../../model/userrole';
import {ServiceAccessMethodType} from '../../../model/service-access-method';
import {AppDeploymentSpec} from '../../../model/app-deployment-spec';
import {AppConfigurationSpec} from '../../../model/app-configuration-spec';
import {ApplicationState} from '../../../model/application-state';
import {AppInstanceStateHistory} from '../../../model/app-instance-state-history';
import {Component, Directive, Input, OnInit, Pipe, PipeTransform} from '@angular/core';
import {Domain} from '../../../model/domain';
import {AccessMethodsModalComponent} from '../modals/access-methods-modal/access-methods-modal.component';
import {ModalComponent} from '../../../shared/modal';
import {AppInstanceExtended} from '../../../model/app-instance-extended';
import {ActivatedRoute} from '@angular/router';
import {ShellClientService} from '../../../service/shell-client.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ApplicationBase} from '../../../model/application-base';
import {Application} from '../../../model/application';
import {ApplicationDTO} from '../../../model/application-dto';

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
class RateComponentMockComponent {
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
    selector: 'nmaas-appinstanceprogress',
    template: '<p>App Instance progress Mock</p>'
})
class AppInstanceProgressMockComponent implements OnInit {
    @Input()
    stages: any;
    @Input()
    activeState: any;
    previousState: any;
    public AppInstanceState: any;

    constructor(translate: TranslateService) {
    }

    public ngOnInit() {
    }

    public getTranslateTag(stateProgress): string {
        return ''
    }
}

@Component({
    selector: 'nmaas-modal',
    template: '<p>Nmaas Modal Mock</p>'
})
class MockNmaasModalComponent extends ModalComponent {
}

@Directive({
    selector: '[roles]'
})
class MockRolesDirective {
    @Input()
    public roles: string[] = []
}

@Component({
    selector: 'app-ssh-shell',
    template: '<p>SSH shell mock</p>'
})
class SshShellMockComponent {
}

describe('Component: AppInstance', () => {
    let component: AppInstanceComponent;
    let fixture: ComponentFixture<AppInstanceComponent>;
    let appConfigService: AppConfigService;
    let appsService: AppsService;
    let authService: AuthService;
    let appInstanceService: AppInstanceService;
    let appImageService: AppImagesService;
    let domainService: DomainService;

    const applicationBase: ApplicationBase = {
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
            {id: null, name: 'tag1'}, {id: null, name: 'tag2'}
            ],
        versions: [{
            id: null,
            version: '0.12',
            state: ApplicationState.ACTIVE,
            appVersionId: 1,
        }],
        rate: null,
    };

    const application: Application = {
        id: 1,
        name: 'Oxidized',
        version: '1.0.0',
        owner: 'admin',
        configWizardTemplate: null,
        configUpdateWizardTemplate: null,
        appDeploymentSpec: new AppDeploymentSpec(),
        appConfigurationSpec: new AppConfigurationSpec(),
        state: ApplicationState.ACTIVE,
    }
    application.appDeploymentSpec.exposesWebUI = true;

    const dto: ApplicationDTO = {
        applicationBase, application
    }

    const domain: Domain = {
        id: 4,
        name: 'domain 1',
        codename: 'dom1',
        active: true,
        domainDcnDetails: null,
        domainTechDetails: null,
        applicationStatePerDomain: [
            {
                applicationBaseId: 2,
                applicationBaseName: 'Oxidized',
                enabled: true,
                pvStorageSizeLimit: 20
            }
        ]
    };

    const appInstance: AppInstanceExtended = {
        applicationId: 2,
        applicationName: 'Oxidized',
        configWizardTemplate: {
            id: 1,
            template: JSON.parse('{"title": "My Test Form","components": [{"type": "textfield", "input": true, "tableView": true, "inputType": "text", "inputMask": "", "label": "First Name", "key": "firstName", "placeholder": "Enter your first name", "prefix": "", "suffix": "", "multiple": false,"defaultValue": "","protected": false,"unique": false,"persistent": true,"validate": {"required": true,"minLength": 2,"maxLength": 10,"pattern": "","custom": "","customPrivate": false},"conditional": {"show": "","when": null,"eq": ""}},{"type": "textfield","input": true,"tableView": true,"inputType": "text","inputMask": "","label": "Last Name","key": "lastName","placeholder": "Enter your last name","prefix": "","suffix": "","multiple": false,"defaultValue": "","protected": false,"unique": false,"persistent": true,"validate": {"required": true, "minLength": 2, "maxLength": 10, "pattern": "", "custom": "", "customPrivate": false}, "conditional": {"show": "", "when": null, "eq": ""}}, {"input": true, "label": "Submit", "tableView": false, "key": "submit", "size": "md", "leftIcon": "", "rightIcon": "", "block": false, "action": "submit", "disableOnInvalid": true, "theme": "primary", "type": "button"}]}')
        },
        configUpdateWizardTemplate: {
            id: 2,
            template: JSON.parse('{"title": "My Test Form","components": [{"type": "textfield", "input": true, "tableView": true, "inputType": "text", "inputMask": "", "label": "First Name", "key": "firstName", "placeholder": "Enter your first name", "prefix": "", "suffix": "", "multiple": false,"defaultValue": "","protected": false,"unique": false,"persistent": true,"validate": {"required": true,"minLength": 2,"maxLength": 10,"pattern": "","custom": "","customPrivate": false},"conditional": {"show": "","when": null,"eq": ""}},{"type": "textfield","input": true,"tableView": true,"inputType": "text","inputMask": "","label": "Last Name","key": "lastName","placeholder": "Enter your last name","prefix": "","suffix": "","multiple": false,"defaultValue": "","protected": false,"unique": false,"persistent": true,"validate": {"required": true, "minLength": 2, "maxLength": 10, "pattern": "", "custom": "", "customPrivate": false}, "conditional": {"show": "", "when": null, "eq": ""}}, {"input": true, "label": "Submit", "tableView": false, "key": "submit", "size": "md", "leftIcon": "", "rightIcon": "", "block": false, "action": "submit", "disableOnInvalid": true, "theme": "primary", "type": "button"}]}')
        },
        configuration: '{"oxidizedUsername":"oxidized","oxidizedPassword":"oxi@PLLAB","targets":[{"ipAddress":"10.0.0.1"},{"ipAddress":"10.0.0.2"},{"ipAddress":"10.0.0.3"},{"ipAddress":"10.0.0.4"},{"ipAddress":"10.0.0.5"},{"ipAddress":"10.0.0.6"},{"ipAddress":"10.0.0.7"},{"ipAddress":"10.0.0.8"},{"ipAddress":"10.0.0.9"},{"ipAddress":"10.0.0.10"},{"ipAddress":"10.0.0.11"},{"ipAddress":"10.0.0.12"},{"ipAddress":"10.0.0.13"},{"ipAddress":"10.0.0.14"},{"ipAddress":"10.0.0.15"},{"ipAddress":"10.0.0.16"}]}',
        createdAt: new Date(),
        descriptiveDeploymentId: 'test-oxidized-48',
        domainId: 4,
        id: 1,
        internalId: 'eccbaf70-7fdd-401a-bb3e-b8659bcfbdff',
        name: 'oxi-virt-1',
        owner: {
            id: 1, username: 'admin', enabled: true,
            firstname: null, lastname: null,
            email: 'admin@admi.eu', selectedLanguage: 'en',
            privacyPolicyAccepted: true, ssoUser: false,
            termsOfUseAccepted: false, roles: [{domainId: 1, role: Role.ROLE_SYSTEM_ADMIN}]
        } as User,
        state: AppInstanceState.RUNNING,
        serviceAccessMethods: [
            {type: ServiceAccessMethodType.DEFAULT, name: 'Default link', protocol: 'Web', url: 'http://oxi-virt-1.test.nmaas.geant.org'},
            {type: ServiceAccessMethodType.EXTERNAL, name: 'Second link', protocol: 'Web', url: 'http://second.org'},
            {type: ServiceAccessMethodType.INTERNAL, name: 'Internal', protocol: 'SSH', url: 'internal'}
        ],
        userFriendlyState: 'Application instance is running',
        application: dto,
        domain: domain,
        appConfigRepositoryAccessDetails: {
            cloneUrl: 'http://clone.me'
        },
        members: []
    };

    const appInstanceHistory: AppInstanceStateHistory[] = [
        {
            timestamp: new Date(2020, 1, 1),
            previousState: 'preparation',
            currentState: 'running'
        },
        {
            timestamp: new Date(2019, 10, 23),
            previousState: 'waiting',
            currentState: 'preparation'
        },
    ];

    beforeEach(async () => {
        const mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getApiUrl', 'getHttpTimeout']);
        mockAppConfigService.getApiUrl.and.returnValue('http://localhost/api');
        mockAppConfigService.getHttpTimeout.and.returnValue(10000);

        const mockShellClientService = jasmine.createSpyObj('ShellClientService', ['getPossiblePods']);
        mockShellClientService.getPossiblePods.and.returnValue(of([]))

        // https://v7.angular.io/guide/testing#component-with-a-dependency
        const appsServiceStub: Partial<AppsService> = {};
        // const authServiceStub: Partial<AuthService> = {};
        const appInstanceServiceStub: Partial<AppInstanceService> = {};
        const domainServiceStub: Partial<DomainService> = {};
        const appImagesServiceStub: Partial<AppImagesService> = {};

        const authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername', 'hasRole', 'hasDomainRole']);
        authServiceSpy.getUsername.and.returnValue('username');
        authServiceSpy.hasRole.and.returnValue(false);
        authServiceSpy.hasDomainRole.and.returnValue(false);

        await TestBed.configureTestingModule({
            declarations: [
                AppInstanceComponent,
                AppRestartModalComponent,
                AppAbortModalComponent,
                SecurePipeMock,
                RateComponentMockComponent,
                AppInstanceProgressMockComponent,
                MockNmaasModalComponent,
                AccessMethodsModalComponent,
                MockRolesDirective,
                SshShellMockComponent,
            ],
            imports: [
                FormsModule,
                HttpClientTestingModule,
                TooltipModule,
                NgxPaginationModule,
                PipesModule,
                FormioModule,
                RouterTestingModule,
                StorageServiceModule,
                JwtModule.forRoot({}),
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })
            ],
            providers: [
                {provide: AppConfigService, useValue: mockAppConfigService},
                {provide: AppsService, useValue: appsServiceStub},
                { provide: AuthService, useValue: authServiceSpy },
                {provide: AppInstanceService, useValue: appInstanceServiceStub},
                {provide: DomainService, useValue: domainServiceStub},
                {provide: AppImagesService, useValue: appImagesServiceStub},
                {provide: ShellClientService, useValue: mockShellClientService},
                {provide: ActivatedRoute, useValue: {params: of({id: 1})}}
            ]
        }).compileComponents().then((result) => {
            console.log(result);
        });
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AppInstanceComponent);
        component = fixture.componentInstance;
        component.appInstanceProgress =
            TestBed.createComponent(AppInstanceProgressMockComponent).componentInstance as AppInstanceProgressComponent;
        component.undeployModal =
            TestBed.createComponent(MockNmaasModalComponent).componentInstance as ModalComponent;

        appConfigService = fixture.debugElement.injector.get(AppConfigService);
        appsService = fixture.debugElement.injector.get(AppsService);
        authService = fixture.debugElement.injector.get(AuthService);
        appInstanceService = fixture.debugElement.injector.get(AppInstanceService);
        appImageService = fixture.debugElement.injector.get(AppImagesService);
        domainService = fixture.debugElement.injector.get(DomainService);

        spyOn(appsService, 'getApplicationDTO').and.returnValue(of(application));
        spyOn(appsService, 'getAppCommentsByUrl').and.returnValue(of([]));
        spyOn(appInstanceService, 'getAppInstance').and.returnValue(of(appInstance));
        spyOn(appInstanceService, 'getAppInstanceHistory').and.returnValue(of(appInstanceHistory));
        spyOn(appInstanceService, 'getAppInstanceState').and.returnValue(of(
            {
                appInstanceId: 48,
                state: AppInstanceState.RUNNING,
                previousState: AppInstanceState.DEPLOYING,
                details: 'Important details',
                userFriendlyDetails: 'User friendly details',
                userFriendlyState: 'User friendly state'
            }
        ));
        spyOn(appImageService, 'getAppLogoUrl').and.returnValue('');
        // spyOn(authService, 'getUsername').and.returnValue('username');

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeDefined();
    });

    it('should create app', () => {
        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    });

    it('title should contain app instance name and app name', () => {
        const element: HTMLElement = fixture.nativeElement;
        const h2 = element.querySelector('h2');
        expect(h2.textContent).toContain(application.name);
        expect(h2.textContent).toContain(appInstance.name);
    });

    it('should transform string to AppInstanceState', () => {
        expect(component.getStateAsEnum(AppInstanceState.DONE)).toEqual(AppInstanceState.DONE);
        expect(component.getStateAsEnum('DONE')).toEqual(AppInstanceState.DONE);
    });

    it('app instance state should be RUNNING', () => {
        expect(component.appInstanceStatus).toBeDefined();
        expect(component.appInstanceStatus.state).toEqual(AppInstanceState.RUNNING);
    });


});
