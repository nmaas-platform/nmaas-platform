import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AboutComponent } from './about.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {NavbarComponent} from "../navbar";
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
import {RecaptchaModule} from "ng-recaptcha";
import {TooltipModule} from "ng2-tooltip-directive";

describe('AboutComponent', () => {
  let component: AboutComponent;
  let fixture: ComponentFixture<AboutComponent>;
  let contentService: ContentDisplayService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AboutComponent, NavbarComponent, ChangelogComponent, FooterComponent, ModalComponent ],
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
          RecaptchaModule,
          TooltipModule
      ],
      providers: [ChangelogService, AppConfigService, ContentDisplayService, NotificationService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AboutComponent);
    component = fixture.componentInstance;
    contentService = fixture.debugElement.injector.get(ContentDisplayService);
    spyOn(contentService, 'getLanguages').and.returnValue(of(['en', 'fr', 'pl']));
    fixture.debugElement.injector.get(TranslateService).use("en");
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
