import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UsersListComponent} from './userslist.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {DomainService, UserService} from '../../../service';
import {UserDataService} from '../../../service/userdata.service';
import {AuthService} from '../../../auth/auth.service';
import {FormsModule} from '@angular/forms';
import {JwtModule} from '@auth0/angular-jwt';
import {NgxPaginationModule} from 'ngx-pagination';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import createSpyObj = jasmine.createSpyObj;

describe('UserslistComponent', () => {
    let component: UsersListComponent;
    let fixture: ComponentFixture<UsersListComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole', 'hasDomainRole']);
        authServiceSpy.hasRole.and.returnValue(true)
        authServiceSpy.hasDomainRole.and.returnValue(true)

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId', 'getAll', 'getMyDomains'])
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)
        domainServiceSpy.getAll.and.returnValue(of([]))
        domainServiceSpy.getMyDomains.and.returnValue(of([]))

        TestBed.configureTestingModule({
            declarations: [UsersListComponent],
            imports: [
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    },
                }),
                JwtModule.forRoot({
                    config: {
                        tokenGetter: () => {
                            return '';
                        }
                    }
                }),
                FormsModule,
                NgxPaginationModule,
                RouterTestingModule
            ],
            providers: [
                {provide: DomainService, useValue: domainServiceSpy},
                {provide: AuthService, useValue: authServiceSpy},
                {
                    provide: UserDataService, useValue: {
                        selectedDomainId: of(1)
                    }
                },
                {provide: UserService, useValue: {}},
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
