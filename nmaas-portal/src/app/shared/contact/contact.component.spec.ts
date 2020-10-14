import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContactComponent} from './contact.component';
import {ModalComponent} from '../modal';
import {RouterTestingModule} from '@angular/router/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';
import {TooltipModule} from 'ng2-tooltip-directive';

describe('ContactComponent', () => {
    let component: ContactComponent;
    let fixture: ComponentFixture<ContactComponent>;

    const recaptchaSpy = createSpyObj('ReCaptchaV3Service', ['execute'])
    recaptchaSpy.execute.and.returnValue(of('TOKEN'))

    const notificationServiceSpy = createSpyObj('NotificationService', ['sendMail'])
    notificationServiceSpy.sendMail.and.returnValue(of({}))

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ContactComponent, ModalComponent],
            imports: [
                RouterTestingModule,
                ReactiveFormsModule,
                FormsModule,
                TooltipModule,
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
        component.mailForm.get('email').setValue('mail@man.poznan.pl')
        component.mailForm.get('name').setValue('Mail')
        component.mailForm.get('message').setValue('TEST')

        component.sendMail();

        expect(notificationServiceSpy.sendMail).toHaveBeenCalledTimes(1);
    })
});
