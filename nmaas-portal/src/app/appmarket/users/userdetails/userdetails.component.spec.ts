import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UserDetailsComponent} from './userdetails.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RouterTestingModule} from '@angular/router/testing';
import {UserService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import createSpyObj = jasmine.createSpyObj;
import {Component} from '@angular/core';

@Component({
    selector: 'nmaas-userdetails',
    template: '<p>Nmaas Userdetails Mock</p>'
})
class MockNmaasUserDetailsComponent {}

@Component({
    selector: 'nmaas-userprivileges',
    template: '<p>Nmaas User Privileges Mock</p>'
})
class MockNmaasUserPrivilegesComponent {}

describe('UserDetailsComponent', () => {
    let component: UserDetailsComponent;
    let fixture: ComponentFixture<UserDetailsComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        TestBed.configureTestingModule({
            declarations: [
                UserDetailsComponent,
                MockNmaasUserDetailsComponent,
                MockNmaasUserPrivilegesComponent
            ],
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
                {provide: UserService, useValue: {}},
                {provide: AuthService, useValue: authServiceSpy},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(UserDetailsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
