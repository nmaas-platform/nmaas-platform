import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UsersListComponent} from './userslist.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppsService, DomainService, UserService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {UserDataService} from '../../../service/userdata.service';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import createSpyObj = jasmine.createSpyObj;

describe('UsersListComponent', () => {
    let component: UsersListComponent;
    let fixture: ComponentFixture<UsersListComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        const userServiceSpy = createSpyObj('UserService', ['getAll'])
        userServiceSpy.getAll.and.returnValue(of([]))

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId'])
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)

        TestBed.configureTestingModule({
            declarations: [UsersListComponent],
            imports: [
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
                {provide: UserService, useValue: userServiceSpy},
                {provide: AuthService, useValue: authServiceSpy},
                {provide: AppsService, useValue: {}},
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
        fixture = TestBed.createComponent(UsersListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
