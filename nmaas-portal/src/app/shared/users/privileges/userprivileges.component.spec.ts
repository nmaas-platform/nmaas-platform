import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UserPrivilegesComponent} from './userprivileges.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DomainService, UserService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {UserDataService} from '../../../service/userdata.service';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RouterTestingModule} from '@angular/router/testing';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('UserPrivilegesComponent', () => {
    let component: UserPrivilegesComponent;
    let fixture: ComponentFixture<UserPrivilegesComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId', 'getAll'])
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)
        domainServiceSpy.getAll.and.returnValue(of([]))

        TestBed.configureTestingModule({
            declarations: [UserPrivilegesComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
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
                {
                    provide: UserDataService,
                    useValue: {
                        selectedDomainId: of(1)
                    }
                },
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(UserPrivilegesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
