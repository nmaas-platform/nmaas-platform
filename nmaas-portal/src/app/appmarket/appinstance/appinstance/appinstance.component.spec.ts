/* tslint:disable:no-unused-variable */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {AppInstanceComponent} from './appinstance.component';
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {JwtModule} from "@auth0/angular-jwt";
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {AppConfigService, AppImagesService, AppInstanceService, AppsService, DomainService} from "../../../service";
import {AuthService} from "../../../auth/auth.service";
import {of} from "rxjs";
import {FormioModule} from "angular-formio";
import {AppInstanceProgressComponent} from "../appinstanceprogress";
import {PipesModule} from "../../../pipe/pipes.module";
import {TooltipModule} from "ng2-tooltip-directive";
import {NgxPaginationModule} from "ngx-pagination";
import {AppRestartModalComponent} from "../modals/apprestart";
import {RouterTestingModule} from "@angular/router/testing";
import {StorageServiceModule} from "ngx-webstorage-service";
import {AppInstance, AppInstanceState, Application, User} from "../../../model";
import {Role} from "../../../model/userrole";
import {ServiceAccessMethodType} from "../../../model/serviceaccessmethod";
import {AppDeploymentSpec} from "../../../model/appdeploymentspec";
import {AppConfigurationSpec} from "../../../model/appconfigurationspec";
import {ApplicationState} from "../../../model/applicationstate";
import {AppInstanceStateHistory} from "../../../model/appinstancestatehistory";
import {Component, Input, Pipe, PipeTransform} from "@angular/core";
import {Domain} from "../../../model/domain";
import {AccessMethodsModalComponent} from "../modals/access-methods-modal/access-methods-modal.component";

@Pipe({
  name: "secure"
})
class SecurePipeMock implements PipeTransform {
  public name: string = "secure";

  public transform(query: string, ...args: any[]): any {
    return query;
  }
}

@Component({
  selector: 'rate',
  template: '<p>Rate Mock</p>'
})
class RateComponentMock {
  @Input()
  private pathUrl:string;
  @Input()
  editable: boolean = false;
  @Input()
  short: boolean = false;
  @Input()
  showVotes: boolean = false;
}

@Component({
  selector: 'nmaas-appinstanceprogress',
  template: '<p>App Instance progress Mock</p>'
})
class AppInstanceProgressMock {
  @Input()
  stages: any;
  @Input()
  activeState: any;
  previousState: any;
  public AppInstanceState: any;
  constructor(translate: TranslateService) {
  }

  public ngOnInit(){}
  public getTranslateTag(stateProgress): string{return ''}
}

@Component({
  selector: 'nmaas-modal',
  template: '<p>Nmaas Modal Mock</p>'
})
class NmaasModalMock{
  @Input()
  styleModal: string;
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

  let appInstance: AppInstance = {
    applicationId: 2,
    applicationName: "Oxidized",
    configWizardTemplate: {template: '{"template":"xD"}'},
    configuration: '{"oxidizedUsername":"oxidized","oxidizedPassword":"oxi@PLLAB","targets":[{"ipAddress":"10.0.0.1"},{"ipAddress":"10.0.0.2"},{"ipAddress":"10.0.0.3"},{"ipAddress":"10.0.0.4"},{"ipAddress":"10.0.0.5"},{"ipAddress":"10.0.0.6"},{"ipAddress":"10.0.0.7"},{"ipAddress":"10.0.0.8"},{"ipAddress":"10.0.0.9"},{"ipAddress":"10.0.0.10"},{"ipAddress":"10.0.0.11"},{"ipAddress":"10.0.0.12"},{"ipAddress":"10.0.0.13"},{"ipAddress":"10.0.0.14"},{"ipAddress":"10.0.0.15"},{"ipAddress":"10.0.0.16"}]}',
    createdAt: new Date(),
    descriptiveDeploymentId: "test-oxidized-48",
    domainId: 4,
    id: 48,
    internalId: "eccbaf70-7fdd-401a-bb3e-b8659bcfbdff",
    name: "oxi-virt-1",
    owner: {
      id: 1, username: "admin", enabled: true,
      firstname: null, lastname: null,
      email: 'admin@admi.eu', selectedLanguage: "en",
      privacyPolicyAccepted: true, ssoUser: false,
      termsOfUseAccepted: false, roles: [{domainId: 1, role: Role.ROLE_SYSTEM_ADMIN}]
    } as User,
    state: AppInstanceState.RUNNING,
    serviceAccessMethods: [
      {type: ServiceAccessMethodType.DEFAULT, name: "Default link", url: "http://oxi-virt-1.test.nmaas.geant.org"},
      {type: ServiceAccessMethodType.EXTERNAL, name: "Second link", url: "http://second.org"},
      {type: ServiceAccessMethodType.INTERNAL, name: "Internal", url: "internal"}
    ],
    userFriendlyState: "Application instance is running"
  };

  let application: Application = {
    id: 2,
    appVersionId: 1,
    name: "Oxidized",
    version: "1.0.0",
    license: null,
    licenseUrl: null,
    wwwUrl: null,
    sourceUrl: null,
    issuesUrl: null,
    owner: "admin",
    descriptions: [],
    tags: ['tag1', 'tag2'],
    appVersions: [],
    configWizardTemplate: null,
    configUpdateWizardTemplate: null,
    appDeploymentSpec: new AppDeploymentSpec(),
    appConfigurationSpec: new AppConfigurationSpec(),
    state: ApplicationState.ACTIVE,
    rowWithVersionVisible: false
  };
  application.appDeploymentSpec.exposesWebUI = true;

  let appInstanceHistory: AppInstanceStateHistory[] = [
    {
      timestamp: new Date(2020,1,1),
      previousState: 'preparation',
      currentState: 'running'
    },
    {
      timestamp: new Date(2019,10,23),
      previousState: 'waiting',
      currentState: 'preparation'
    },
  ];

  let domain: Domain = {
    id: 4,
    name: 'domain 1',
    codename: 'dom1',
    active: true,
    domainDcnDetails: null,
    domainTechDetails: null,
    applicationStatePerDomain: [
      {
        applicationBaseId: 2,
        applicationBaseName: "Oxidized",
        enabled: true,
        pvStorageSizeLimit: 20
      }
    ]
  };

  // https://angular.io/guide/testing#component-with-a-dependency
  let appsServiceStub: Partial<AppsService>;
  let authServiceStub: Partial<AuthService>;
  let appConfigServiceStub: Partial<AppConfigService>;
  let appInstanceServiceStub: Partial<AppInstanceService>;
  let domainServiceStub: Partial<DomainService>;
  let appImagesServiceStub: Partial<AppImagesService>;

  beforeEach(async (()=>{
    let mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getApiUrl', 'getHttpTimeout']);
    mockAppConfigService.getApiUrl.and.returnValue('http://localhost/api');
    mockAppConfigService.getHttpTimeout.and.returnValue(10000);
    TestBed.configureTestingModule({
      declarations: [
          AppInstanceComponent,
        AppRestartModalComponent,
        SecurePipeMock,
        RateComponentMock,
        AppInstanceProgressMock,
        NmaasModalMock,
          AccessMethodsModalComponent,
      ],
      imports:[
        FormsModule,
        HttpClientModule,
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
        { provide: AppsService, useValue: appsServiceStub },
        { provide: AuthService, useValue: authServiceStub },
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: AppInstanceService, useValue: appInstanceServiceStub },
        { provide: DomainService, useValue: domainServiceStub },
        { provide: AppImagesService, useValue: appImagesServiceStub }
      ]
    }).compileComponents().then((result) => {
      console.log(result);
    });
  }));

  beforeEach(()=>{
    fixture = TestBed.createComponent(AppInstanceComponent);
    component = fixture.componentInstance;
    component.appInstanceProgress = TestBed.createComponent(AppInstanceProgressMock).componentInstance as AppInstanceProgressComponent;
    appConfigService = fixture.debugElement.injector.get(AppConfigService);
    appsService = fixture.debugElement.injector.get(AppsService);
    authService = fixture.debugElement.injector.get(AuthService);
    appInstanceService = fixture.debugElement.injector.get(AppInstanceService);
    appImageService = fixture.debugElement.injector.get(AppImagesService);
    domainService = fixture.debugElement.injector.get(DomainService);
    // optional in future
    // spyOn(appConfigService, 'getApiUrl').and.returnValue("http://localhost/api");
    spyOn(appsService, 'getApp').and.returnValue(of(application));
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
    // optional in future
    // spyOn(domainService, 'getOne').and.returnValue(of(domain));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should create app', ()=>{
    let app = fixture.debugElement.componentInstance;
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
