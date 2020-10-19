import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContactComponent} from './contact.component';
import {ModalComponent} from '../modal';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';
import {FormioModule} from 'angular-formio';
import {ContactFormService} from '../../service/contact-form.service';
import {EventEmitter} from '@angular/core';
import {FormsModule} from '@angular/forms';

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
        ['CONTACT', null],
        ['ISSUES', null]
    ])));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ContactComponent, ModalComponent],
            imports: [
                FormioModule,
                FormsModule,
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

        expect(notificationServiceSpy.sendMail).toHaveBeenCalledTimes(1);
    })
});
