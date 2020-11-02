import {PasswordResetComponent} from './password-reset.component';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {UserService} from '../../service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ModalComponent} from '../../shared/modal';
import {RouterTestingModule} from '@angular/router/testing';
import {PasswordStrengthMeterModule} from 'angular-password-strength-meter';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('Password reset component', () => {
    let component: PasswordResetComponent;
    let fixture: ComponentFixture<PasswordResetComponent>;

    beforeEach(async(() => {
        const userServiceSpy = createSpyObj('UserService', ['validateResetRequest'])
        userServiceSpy.validateResetRequest.and.returnValue(of({}))

        TestBed.configureTestingModule({
            declarations: [PasswordResetComponent, ModalComponent],
            imports: [
                RouterTestingModule,
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
            providers: [
                {provide: UserService, useValue: userServiceSpy},
                {provide: ReCaptchaV3Service, useValue: {}},
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(PasswordResetComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create component', () => {
        expect(component).toBeTruthy()
    });
});
