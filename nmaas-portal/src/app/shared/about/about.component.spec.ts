import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AboutComponent } from './about.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {ChangelogComponent} from "../changelog/changelog.component";
import {FooterComponent} from "../footer";
import {AppConfigService, ChangelogService} from "../../service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ContentDisplayService} from "../../service/content-display.service";
import {RouterTestingModule} from "@angular/router/testing";
import {of} from "rxjs";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalComponent} from "../modal";
import {NotificationService} from "../../service/notification.service";
import {RECAPTCHA_V3_SITE_KEY, RecaptchaModule, ReCaptchaV3Service} from "ng-recaptcha";
import {TooltipModule} from "ng2-tooltip-directive";
import {Component} from "@angular/core";
import {InternationalizationService} from "../../service/internationalization.service";

@Component({
  selector: 'app-navbar',
  template: '<p>Mock app-navbar Component</p>'
})
class MockAppNavbar{}

describe('AboutComponent', () => {
  let component: AboutComponent;
  let fixture: ComponentFixture<AboutComponent>;
  let languageService: InternationalizationService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AboutComponent, ChangelogComponent, FooterComponent, ModalComponent, MockAppNavbar ],
      imports:[
          TranslateModule.forRoot({
            loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
            }
          }),
          HttpClientTestingModule,
          RouterTestingModule,
          FormsModule,
          ReactiveFormsModule,
          TooltipModule
      ],
      providers: [
          ChangelogService,
          AppConfigService,
          ContentDisplayService,
          NotificationService,
          InternationalizationService,
          ReCaptchaV3Service,
          {
              provide: RECAPTCHA_V3_SITE_KEY,
              useFactory: function (appConfigService: AppConfigService) {
                  return appConfigService.getSiteKey();
              },
              deps: [AppConfigService]
          }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AboutComponent);
    component = fixture.componentInstance;
    languageService = fixture.debugElement.injector.get(InternationalizationService);
    spyOn(languageService, 'getEnabledLanguages').and.returnValue(of(['en', 'fr', 'pl']));
    fixture.debugElement.injector.get(TranslateService).use("en");
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
