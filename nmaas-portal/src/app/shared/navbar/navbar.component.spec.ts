import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NavbarComponent} from './navbar.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {Observable, of} from "rxjs";
import {RouterTestingModule} from "@angular/router/testing";
import {AuthService, DomainRoles} from "../../auth/auth.service";
import {AppConfigService, ConfigurationService, DomainService} from "../../service";
import {RolesDirective} from "../../directive/roles.directive";
import {DomainFilterComponent} from "../common/domainfilter/domainfilter.component";
import {Component} from "@angular/core";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {Content} from "../../model/content";

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

class MockAuthService{

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

class MockDomainService{

}

describe('NavbarComponent_Shared', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let contentService: ContentDisplayService;
  let spy: any;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        NavbarComponent,
        RolesDirective,
        MockDomainFilter
      ],
        imports: [
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
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    contentService = fixture.debugElement.injector.get(ContentDisplayService);
    spy = spyOn(contentService, 'getLanguages').and.returnValue(of(['en', 'fr', 'pl']));
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
