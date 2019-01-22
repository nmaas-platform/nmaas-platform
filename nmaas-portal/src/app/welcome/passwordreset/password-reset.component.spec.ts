import {PasswordResetComponent} from "./password-reset.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {AppConfigService, ChangelogService, DomainService, UserService} from "../../service";
import {NavbarComponent} from "../../shared/navbar";
import {FooterComponent} from "../../shared/footer";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalComponent} from "../../shared/modal";
import {ChangelogComponent} from "../changelog/changelog.component";
import {ModalChangelogComponent} from "../../shared/footer/modal-changelog/modal-changelog.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {ContentDisplayService} from "../../service/content-display.service";
import {Observable, of} from "rxjs";
import {PasswordStrengthMeterComponent, PasswordStrengthMeterModule} from "angular-password-strength-meter";

describe('Password reset component', () =>{
   let component: PasswordResetComponent;
   let fixture: ComponentFixture<PasswordResetComponent>;
   let contentService: ContentDisplayService;

   beforeEach(async (() =>{
       TestBed.configureTestingModule({
           declarations: [PasswordResetComponent, NavbarComponent, FooterComponent, ModalComponent, ModalChangelogComponent],
           imports: [
               RouterTestingModule,
               HttpClientTestingModule,
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
       contentService = fixture.debugElement.injector.get(ContentDisplayService);
       spyOn(contentService, 'getLanguages').and.returnValue(of(['en', 'fr', 'pl']));
       fixture.debugElement.injector.get(TranslateService).use("en");
       fixture.detectChanges();
   });

   it('should create component', () => {
      let app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
   });
});
