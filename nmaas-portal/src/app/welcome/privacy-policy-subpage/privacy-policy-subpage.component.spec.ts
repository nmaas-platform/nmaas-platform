import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PrivacyPolicySubpageComponent } from './privacy-policy-subpage.component';
import {NavbarComponent} from "../../shared/navbar";
import {ContentDisplayService} from "../../service/content-display.service";
import {SharedModule} from "../../shared";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {RouterTestingModule} from "@angular/router/testing";
import {JwtModule} from "@auth0/angular-jwt";
import {Observable, of} from "rxjs";
import {ChangelogService} from "../../service";

describe('PrivacyPolicySubpageComponent', () => {
  let component: PrivacyPolicySubpageComponent;
  let fixture: ComponentFixture<PrivacyPolicySubpageComponent>;
  let contentService: ContentDisplayService;
  let changelogService: ChangelogService;
  let navbar: NavbarComponent;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PrivacyPolicySubpageComponent ],
      imports: [
          SharedModule,
          RouterTestingModule,
          TranslateModule.forRoot({
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
          })
      ],
      providers: [
          ContentDisplayService, ChangelogService, NavbarComponent
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PrivacyPolicySubpageComponent);
    component = fixture.componentInstance;
    contentService = fixture.debugElement.injector.get(ContentDisplayService);
    changelogService = fixture.debugElement.injector.get(ChangelogService);
    navbar = fixture.debugElement.injector.get(NavbarComponent);
    spyOn(contentService, 'getContent').and.returnValue(Observable.empty());
    spyOn(contentService, 'getLanguages').and.returnValue(of(['en', 'fr', 'pl']));
    navbar.useLanguage("en");
    spyOn(changelogService, 'getGitInfo').and.returnValue(Observable.empty());
    spyOn(changelogService, 'getChangelog').and.returnValue(Observable.empty());
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
