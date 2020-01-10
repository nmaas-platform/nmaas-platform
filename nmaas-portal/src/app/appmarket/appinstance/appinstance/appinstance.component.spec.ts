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
import {AppInstance, AppInstanceState, User} from "../../../model";
import {Role} from "../../../model/userrole";
import {ServiceAccessMethodType} from "../../../model/serviceaccessmethod";

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
    configWizardTemplate: null,
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
      {type: ServiceAccessMethodType.DEFAULT, name: "Default", url: "http://oxi-virt-1.test.nmaas.geant.org"},
      {type: ServiceAccessMethodType.EXTERNAL, name: "Second", url: "httpL//second.org"}
    ],
    userFriendlyState: "Application instance is running"
  };

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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should create app', ()=>{
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });


});
