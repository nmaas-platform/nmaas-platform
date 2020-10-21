import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContactComponent} from './contact.component';
import {ModalComponent} from '../modal';
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';
import {of} from 'rxjs';
import {FormioModule} from 'angular-formio';
import {ContactFormService} from '../../service/contact-form.service';
import {EventEmitter} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AuthService} from '../../auth/auth.service';
import {AccessModifier} from '../../model/contact-form-type';
import createSpyObj = jasmine.createSpyObj;
import {InternationalizationService} from '../../service/internationalization.service';

describe('ContactComponent', () => {
    let component: ContactComponent;
    let fixture: ComponentFixture<ContactComponent>;

    const recaptchaSpy = createSpyObj('ReCaptchaV3Service', ['execute'])
    recaptchaSpy.execute.and.returnValue(of('TOKEN'))

    const notificationServiceSpy = createSpyObj('NotificationService', ['sendMail'])
    notificationServiceSpy.sendMail.and.returnValue(of({}))

    const contactFormServiceSpy = createSpyObj('ContactFormService', ['getForm', 'getAllFormTypesAsMap'])
    contactFormServiceSpy.getForm.and.returnValue(of({}))
    contactFormServiceSpy.getAllFormTypesAsMap.and.returnValue(of(new Map([
        ['CONTACT', {key: 'CONTACT', access: AccessModifier.ALL, templateName: 'default'}],
        ['ISSUES', {key: 'ISSUES', access: AccessModifier.ONLY_LOGGED_IN, templateName: 'issues'}],
        ['ACCESS_REQUEST', {key: 'ACCESS_REQUEST', access: AccessModifier.ONLY_NOT_LOGGED_IN, templateName: 'default'}],
        ['ERROR', {key: 'ERROR', access: null}],
    ])));

    const authServiceSpy = createSpyObj('AuthService', ['isLogged']);
    authServiceSpy.isLogged.and.returnValue('false');

    const langServiceSpy = createSpyObj('InternationalizationService', ['getEnabledLanguages', 'getLanguageContent'])
    langServiceSpy.getEnabledLanguages.and.returnValue(of(['en', 'fr', 'ge', 'pl']))
    langServiceSpy.getLanguageContent.and.returnValue(of({CONTACT_FORM: {FORMIO: {MESSAGE: 'Message'}}}))

    beforeEach(async () => {

        await TestBed.configureTestingModule({
            declarations: [ContactComponent, ModalComponent],
            imports: [
                FormioModule,
                FormsModule,
                ReactiveFormsModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: ReCaptchaV3Service, useValue: recaptchaSpy},
                {provide: NotificationService, useValue: notificationServiceSpy},
                {provide: ContactFormService, useValue: contactFormServiceSpy},
                {provide: AuthService, useValue: authServiceSpy},
                {provide: InternationalizationService, useValue: langServiceSpy},
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ContactComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should send mail when valid', () => {

        component.ready({
            'formio': new EventEmitter<any>()
        });

        const data = {
            'email': 'mail@user.com',
            'name': 'TEST',
            'message': 'TEST',
        }

        component.onSubmit(data);

        expect(recaptchaSpy.execute).toHaveBeenCalledTimes(1);
        expect(notificationServiceSpy.sendMail).toHaveBeenCalledTimes(1);
    });

    it('should update formio form after selectForm is updated', () => {
        component.selectForm.get('formKey').setValue('ISSUES')

        expect(contactFormServiceSpy.getForm).toHaveBeenCalledWith('issues');
    });
});
