import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NavbarComponent} from './navbar.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {Observable, of} from "rxjs";
import {RouterTestingModule} from "@angular/router/testing";
import {AuthService} from "../../auth/auth.service";
import {AppConfigService, DomainService} from "../../service";
import {RolesDirective} from "../../directive/roles.directive";
import {Component} from "@angular/core";
import {Content} from "../../model/content";
import {InternationalizationService} from "../../service/internationalization.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

class MockContentDisplayService{

  public getLanguages(): Observable<string[]>{
    return of([]);
  }

  private updateRequiredFlag: boolean = false;

  public getContent(name: string): Observable<Content>{
    return of();
  }

  protected getContentUrl(): string{
    return "";
  }

  public setUpdateRequiredFlag(flag:boolean){
    this.updateRequiredFlag = flag;
  }

  public shouldUpdate(): boolean{
    return this.updateRequiredFlag;
  }
}

export class MockAuthService{

  public loginUsingSsoService: boolean;

  constructor() {}

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

@Component({
  selector: 'nmaas-domain-filter',
  template: '<p>Mock nmaas-domain-filter Component</p>'
})
class MockDomainFilter{}

@Component({
  selector: 'app-modal-notification-send',
  template: '<p>Mock modal</p>'
})
class MockNotificationSendModalComponent {}

class MockDomainService{

}

describe('NavbarComponent_Shared', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let languageService: InternationalizationService;
  let spy: any;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        NavbarComponent,
        RolesDirective,
        MockDomainFilter,
        MockNotificationSendModalComponent
      ],
        imports: [
            HttpClientTestingModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            }),
            RouterTestingModule
        ],
        providers: [
          {provide: ContentDisplayService, useClass: MockContentDisplayService},
          {provide: AuthService, useClass: MockAuthService},
          {provide: DomainService, useClass: MockDomainService},
          InternationalizationService,
          AppConfigService
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    languageService = fixture.debugElement.injector.get(InternationalizationService);
    spy = spyOn(languageService, 'getEnabledLanguages').and.returnValue(of(['en', 'fr', 'pl']));
    component.useLanguage('en');
    fixture.detectChanges();
  });

  it('should create', () => {
      expect(component).toBeTruthy();
  });

  it('should change language',() =>{
      component.useLanguage("fr");
      expect(component.getCurrent()).toBe("fr");
  })
});
