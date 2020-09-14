import {PasswordResetComponent} from "./password-reset.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {AppConfigService, ChangelogService, DomainService, UserService} from "../../service";
import {NavbarComponent} from "../../shared/navbar";
import {FooterComponent} from "../../shared/footer";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalComponent} from "../../shared/modal";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {ContentDisplayService} from "../../service/content-display.service";
import {of} from "rxjs";
import {PasswordStrengthMeterModule} from "angular-password-strength-meter";
import {InternationalizationService} from "../../service/internationalization.service";
import {RecaptchaFormsModule} from 'ng-recaptcha';

describe('Password reset component', () =>{
   let component: PasswordResetComponent;
   let fixture: ComponentFixture<PasswordResetComponent>;
   let languageService: InternationalizationService;

   beforeEach(async (() =>{
       TestBed.configureTestingModule({
           declarations: [PasswordResetComponent, NavbarComponent, FooterComponent, ModalComponent],
           imports: [
               RouterTestingModule,
               HttpClientTestingModule,
               RecaptchaFormsModule,
               FormsModule,
               ReactiveFormsModule,
               TranslateModule.forRoot({
                   loader: {
                       provide: TranslateLoader,
                       useClass: TranslateFakeLoader
                   }
               }),
             PasswordStrengthMeterModule
           ],
           providers: [UserService, AppConfigService, DomainService, ContentDisplayService, ChangelogService]
       }).compileComponents();
   }));

   beforeEach(() => {
       fixture = TestBed.createComponent(PasswordResetComponent);
       component = fixture.componentInstance;
       languageService = fixture.debugElement.injector.get(InternationalizationService);
       spyOn(languageService, 'getEnabledLanguages').and.returnValue(of(['en', 'fr', 'pl']));
       fixture.debugElement.injector.get(TranslateService).use("en");
       fixture.detectChanges();
   });

   // it('should create component', () => {
   //  let app = fixture.debugElement.componentInstance;
   //   expect(app).toBeTruthy();
   // });
});
