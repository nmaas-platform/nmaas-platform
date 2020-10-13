import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationComponent} from './registration.component';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RegistrationService} from '../../auth/registration.service';
import {AppConfigService} from '../../service';
import {ModalComponent} from '../../shared/modal';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('RegistrationComponent', () => {
    let component: RegistrationComponent;
    let fixture: ComponentFixture<RegistrationComponent>;

    beforeEach(async(() => {
        const registrationServiceSpy = createSpyObj('RegistrationService', ['getDomains']);
        registrationServiceSpy.getDomains.and.returnValue(of([]));

        TestBed.configureTestingModule({
            declarations: [RegistrationComponent, ModalComponent],
            imports: [
                ReactiveFormsModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: RegistrationService, useValue: registrationServiceSpy},
                {provide: AppConfigService, useValue: {}},
                {provide: ReCaptchaV3Service, useValue: {}},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RegistrationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
