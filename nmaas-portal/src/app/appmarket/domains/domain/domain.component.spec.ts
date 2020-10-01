import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DomainComponent} from './domain.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppsService, DomainService, UserService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {SharedModule} from '../../../shared';
import {FormsModule} from '@angular/forms';
import createSpyObj = jasmine.createSpyObj;

describe('DomainComponent', () => {
    let component: DomainComponent;
    let fixture: ComponentFixture<DomainComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId'])
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)

        TestBed.configureTestingModule({
            declarations: [DomainComponent],
            imports: [
                SharedModule,
                FormsModule,
                RouterTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: DomainService, useValue: domainServiceSpy},
                {provide: UserService, useValue: {}},
                {provide: AuthService, useValue: authServiceSpy},
                {provide: AppsService, useValue: {}},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(DomainComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
