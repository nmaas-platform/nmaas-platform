/* tslint:disable:no-unused-variable */

import {TestBed, async, ComponentFixture} from '@angular/core/testing';
import { AppDetailsComponent } from './appdetails.component';
import {RouterTestingModule} from "@angular/router/testing";
import {AppConfigService, AppImagesService, AppInstanceService, AppsService, DomainService} from "../../service";
import {Component, Input, Pipe, PipeTransform} from "@angular/core";
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {AppSubscriptionsService} from "../../service/appsubscriptions.service";
import {UserDataService} from "../../service/userdata.service";
import {AuthService} from "../../auth/auth.service";
import {TooltipModule} from "ng2-tooltip-directive";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ActivatedRoute} from "@angular/router";
import {of} from "rxjs";
import {Application} from "../../model";
import {AppDeploymentSpec} from "../../model/appdeploymentspec";
import {AppConfigurationSpec} from "../../model/appconfigurationspec";
import {ApplicationState} from "../../model/applicationstate";

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
  selector: 'rating-extended',
  template: '<p>Rate Extended mock</p>'
})
class RateExtendedMock{
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
  selector: 'comments',
  template: '<p>Mock comments component</p>'
})
class CommentsMock {
  @Input()
  pathUrl: string;
}

@Component({
  selector: 'nmaas-modal-app-install',
  template: '<p>Nmaas modal app install mock</p>'
})
class NmassModalAppInstallMock {
  @Input()
  app: any;
}

@Component({
  selector: 'screenshots',
  template: '<p>Screenchots Mock</p>'
})
class ScreenshotsMock{
  @Input()
  pathUrl: string;
}

describe('Component: AppDetails', () => {
  let component: AppDetailsComponent;
  let fixture: ComponentFixture<AppDetailsComponent>;

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
    nmaasDocumentationUrl: null,
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

  beforeEach(async(() => {
    let appsServiceSpy = jasmine.createSpyObj('AppsService',['getBaseApp']);
    appsServiceSpy.getBaseApp.and.returnValue(of(application));
    let appSubsServiceSpy = jasmine.createSpyObj('AppSubscriptionService', ['getAllByApplication', 'getSubscription', 'unsubscribe']);
    appSubsServiceSpy.getAllByApplication.and.returnValue(of([]));
    let appImagesServiceSpy = jasmine.createSpyObj('AppImagesService', ['getAppLogoUrl']);
    // let appInstanceServiceSpy = jasmine.createSpyObj('AppInstanceService', []);
    let userDataServiceSpy;
    let appConfigSpy = jasmine.createSpyObj('AppConfigService',['getNmaasGlobalDomainId', 'getApiUrl', 'getHttpTimeout']);
    appConfigSpy.getApiUrl.and.returnValue("http://localhost:9000/");
    appConfigSpy.getNmaasGlobalDomainId.and.returnValue(1);
    appConfigSpy.getHttpTimeout.and.returnValue(10000);
    let authServiceSpy = jasmine.createSpyObj('AuthService', ['hasRole', 'hasDomainRole']);
    let domainServiceSpy = jasmine.createSpyObj('DomainService', ['getOne']);

    TestBed.configureTestingModule({
      declarations: [
          AppDetailsComponent,
          SecurePipeMock,
          RateComponentMock,
          CommentsMock,
          NmassModalAppInstallMock,
          RateExtendedMock,
          ScreenshotsMock
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
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  })

});
