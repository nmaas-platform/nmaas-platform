import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PrivacyPolicySubpageComponent } from './privacy-policy-subpage.component';
import {NavbarComponent} from "../../shared/navbar";
import {ContentDisplayService} from "../../service/content-display.service";
import {SharedModule} from "../../shared";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {RouterTestingModule} from "@angular/router/testing";
import {JwtModule} from "@auth0/angular-jwt";
import {EMPTY, of} from "rxjs";
import {AppConfigService, ChangelogService} from "../../service";
import {InternationalizationService} from "../../service/internationalization.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('PrivacyPolicySubpageComponent', () => {
  let component: PrivacyPolicySubpageComponent;
  let fixture: ComponentFixture<PrivacyPolicySubpageComponent>;
  let contentService: ContentDisplayService;
  let languageService: InternationalizationService;
  let changelogService: ChangelogService;
  let navbar: NavbarComponent;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PrivacyPolicySubpageComponent ],
      imports: [
          SharedModule,
          RouterTestingModule,
          HttpClientTestingModule,
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
          ContentDisplayService, ChangelogService, NavbarComponent, InternationalizationService, AppConfigService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PrivacyPolicySubpageComponent);
    component = fixture.componentInstance;
    contentService = fixture.debugElement.injector.get(ContentDisplayService);
    languageService = fixture.debugElement.injector.get(InternationalizationService);
    changelogService = fixture.debugElement.injector.get(ChangelogService);
    navbar = fixture.debugElement.injector.get(NavbarComponent);
    spyOn(contentService, 'getContent').and.returnValue(EMPTY);
    spyOn(languageService, 'getEnabledLanguages').and.returnValue(of(['en', 'fr', 'pl']));
    navbar.useLanguage("en");
    spyOn(changelogService, 'getGitInfo').and.returnValue(EMPTY);
    spyOn(changelogService, 'getChangelog').and.returnValue(EMPTY);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
