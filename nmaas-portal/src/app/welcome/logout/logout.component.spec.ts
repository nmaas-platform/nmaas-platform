import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LogoutComponent} from './logout.component';
import {RouterTestingModule} from '@angular/router/testing';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';
import {AuthService} from '../../auth/auth.service';
import {ConfigurationService} from '../../service';
import {ShibbolethService} from '../../service/shibboleth.service';

describe('LogoutComponent', () => {
    let component: LogoutComponent;
    let fixture: ComponentFixture<LogoutComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['logout'])
        const configServiceSpy = createSpyObj('ConfigurationService', ['getConfiguration'])
        configServiceSpy.getConfiguration.and.returnValue(of({
            ssoLoginAllowed: false
        }));

        TestBed.configureTestingModule({
            declarations: [LogoutComponent],
            imports: [
                RouterTestingModule.withRoutes(
                    [{path: 'welcome', redirectTo: ''}]
                )
            ],
            providers: [
                {provide: AuthService, useValue: authServiceSpy},
                {provide: ConfigurationService, useValue: configServiceSpy},
                {provide: ShibbolethService, useValue: {}}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LogoutComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
