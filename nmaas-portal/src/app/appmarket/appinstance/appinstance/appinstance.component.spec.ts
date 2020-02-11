/* tslint:disable:no-unused-variable */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {AppInstanceComponent} from './appinstance.component';
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {JwtModule} from "@auth0/angular-jwt";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {AppConfigService, AppInstanceService, AppsService} from "../../../service";
import {AuthService} from "../../../auth/auth.service";
import {of} from "rxjs";
import {SharedModule} from "../../../shared";
import {FormioModule} from "angular-formio";
import {AppInstanceProgressComponent} from "../appinstanceprogress";
import {PipesModule} from "../../../pipe/pipes.module";
import {TooltipModule} from "ng2-tooltip-directive";
import {NgxPaginationModule} from "ngx-pagination";
import {AppRestartModalComponent} from "../../modals/apprestart";
import {RouterTestingModule} from "@angular/router/testing";
import {StorageServiceModule} from "ngx-webstorage-service";
import {AppInstance, AppInstanceState, Application, User} from "../../../model";
import {Role} from "../../../model/userrole";
import {ServiceAccessMethodType} from "../../../model/serviceaccessmethod";
import {AppDeploymentSpec} from "../../../model/appdeploymentspec";
import {AppConfigurationSpec} from "../../../model/appconfigurationspec";
import {ApplicationState} from "../../../model/applicationstate";
import {AppInstanceStateHistory} from "../../../model/appinstancestatehistory";

describe('Component: AppInstance', () => {
  let component: AppInstanceComponent;
  let fixture: ComponentFixture<AppInstanceComponent>;
  let appConfigService: AppConfigService;
  let appsService: AppsService;
  let authService: AuthService;
  let appInstanceService: AppInstanceService;

  beforeEach(async (()=>{
    TestBed.configureTestingModule({
      declarations: [AppInstanceComponent, AppInstanceProgressComponent, AppRestartModalComponent],
      imports:[
        FormsModule,
        HttpClientModule,
        TooltipModule,
        NgxPaginationModule,
        PipesModule,
        SharedModule,
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
      providers: [AppsService, AuthService, AppConfigService, AppInstanceService]
    }).compileComponents();
  }));

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
      {type: ServiceAccessMethodType.EXTERNAL, name: "Second link", url: "httpL//second.org"}
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

  beforeEach(()=>{
    fixture = TestBed.createComponent(AppInstanceComponent);
    component = fixture.componentInstance;
    appConfigService = fixture.debugElement.injector.get(AppConfigService);
    appsService = fixture.debugElement.injector.get(AppsService);
    authService = fixture.debugElement.injector.get(AuthService);
    appInstanceService = fixture.debugElement.injector.get(AppInstanceService);
    spyOn(appConfigService, 'getApiUrl').and.returnValue("http://localhost/api/");
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
    spyOn(appsService, 'getApp').and.returnValue(of(application));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should create app', ()=>{
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('app instance state should be RUNNING', () => {
    expect(component.appInstanceStatus).toBeDefined();
    expect(component.appInstanceStatus.state).toEqual(AppInstanceState.RUNNING);
  });

  it('next test', () => {
    let element = fixture.debugElement.nativeElement.querySelector('a.dropdown-item');
    console.log(element);
    expect(element).toBeDefined();
  });


});
