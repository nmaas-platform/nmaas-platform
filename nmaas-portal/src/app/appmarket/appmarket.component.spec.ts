import {TestBed, async} from '@angular/core/testing';
import { AppMarketComponent } from './appmarket.component';
import {RouterTestingModule} from "@angular/router/testing";
import {ServiceUnavailableService} from "../service-unavailable/service-unavailable.service";
import {
  MissingTranslationHandler,
  TranslateFakeLoader,
  TranslateLoader,
  TranslateModule,
  TranslateService
} from "@ngx-translate/core";
import {CustomMissingTranslationService} from "../i18n/custommissingtranslation.service";
import {SharedModule} from "../shared";
import {InternationalizationService} from "../service/internationalization.service";
import {JwtHelperService, JwtModule} from "@auth0/angular-jwt";
import {AppConfigService, ConfigurationService} from "../service";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AuthService} from "../auth/auth.service";
import {Observable, of} from "rxjs";
import {Configuration} from "../model/configuration";
import {Language} from "../model/language";

export class MockAuthService {

  public loginUsingSsoService: boolean;

  constructor() {
  }

  private storeToken(token: string): void {

  }

  private getToken(): string {
    return String("");
  }

  private removeToken(): void {

  }

  public getUsername(): string {
    return String("admin")
  }

  public hasRole(name: string): boolean {
    return false;
  }

  public hasDomainRole(domainId: number, name: string): boolean {
    return false;
  }

  public getRoles(): string[] {
    return [];
  }

  public getDomains(): number[] {
    return [];
  }

  public logout(): void {
    this.removeToken();
  }

  public isLogged(): boolean {
    const token = this.getToken();
    return token != "";

  }
}

class MockServiceUnavailableService {
  public isServiceAvailable: boolean;

  public validateServicesAvailability(){
    this.isServiceAvailable = true;
  }
}

class MockConfigurationService{
  protected uri:string;

  constructor() {
    this.uri = 'http://localhost/api';
  }

  public getApiUrl(): string {
    return 'http://localhost/api';
  }

  public getConfiguration():Observable<Configuration>{
    return of<Configuration>();
  }

  public updateConfiguration(configuration:Configuration):Observable<any>{
    return of<Configuration>();
  }

}

class MockInternationalizationService{
  public updateRequiredFlag: boolean = false;

  constructor() {}

  public saveLanguageContent(language: Language) : Observable<any> {
      return of<any>();
  }

  public getAllSupportedLanguages():Observable<Language[]>{
    return of<any>();
  }

  public getLanguage(language: string): Observable<Language> {
    return of<any>();
  }

  public changeSupportedLanguageState(language: Language):Observable<any>{
    return of<any>();
  }

  public getEnabledLanguages(): Observable<string[]>{
    return of<any>();
  }

  private getInternationalizationUrl(): string{
    return "";
    }

  public setUpdateRequiredFlag(flag:boolean){
      this.updateRequiredFlag = flag;
    }

  public shouldUpdate(): boolean{
      return this.updateRequiredFlag;
    }
}

class MockAppConfigService{
  config: any;

  constructor() { }

  public load() {
  }

  public getApiUrl(): string {
    return "";
  }

  public getNmaasGlobalDomainId(): number {
    return 0;
  }

  public getHttpTimeout(): number {
    return 10000;
  }

  public getShowGitInfo(): boolean {
    return false;
  }

  public getShowChangelog(): boolean {
    return false;
  }
}

describe('Component: AppMarket', () => {
  let component: AppMarketComponent;
  let fixture: any;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppMarketComponent
      ],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot({
          missingTranslationHandler: {provide: MissingTranslationHandler, useClass: CustomMissingTranslationService},
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        }),
        JwtModule.forRoot({
          config: {
            tokenGetter: () => {
              return '';
            }
          }
        }),
        SharedModule
      ],
      providers: [
        {provide: ServiceUnavailableService, useClass: MockServiceUnavailableService},
        {provide: AppConfigService, useClass: MockAppConfigService},
        HttpClient,
        HttpHandler,
        {provide: ConfigurationService, useClass: MockConfigurationService},
        TranslateService,
        {provide: AuthService, useClass: MockAuthService},
        JwtHelperService,
        {provide: InternationalizationService, useClass: MockInternationalizationService}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppMarketComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });
});
