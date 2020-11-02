import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ProfileComponent} from './profile.component';
import {Component} from '@angular/core';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {UserService} from '../../service';
import {AuthService} from '../../auth/auth.service';
import createSpyObj = jasmine.createSpyObj;
import {ProfileService} from '../../service/profile.service';
import {ContentDisplayService} from '../../service/content-display.service';
import {InternationalizationService} from '../../service/internationalization.service';
import {of} from 'rxjs';

@Component({
    selector: 'nmaas-userdetails',
    template: '<p>Nmaas Userdetails Mock</p>'
})
class MockNmaasUserDetailsComponent {
}

@Component({
    selector: 'nmaas-userprivileges',
    template: '<p>Nmaas User Privileges Mock</p>'
})
class MockNmaasUserPrivilegesComponent {
}

@Component({
    selector: 'nmaas-ssh-keys',
    template: '<p>Nmaas ssh keys component</p>'
})
class MockNmaasSshKeysComponent {
}

describe('ProfileComponent', () => {
    let component: ProfileComponent;
    let fixture: ComponentFixture<ProfileComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        const internationalizationSpy = createSpyObj('InternationalizationService', ['getEnabledLanguages'])
        internationalizationSpy.getEnabledLanguages.and.returnValue(of(['en', 'pl']))

        const profileServiceSpy = createSpyObj('ProfileService', ['getOne'])
        profileServiceSpy.getOne.and.returnValue(of())

        TestBed.configureTestingModule({
            declarations: [
                ProfileComponent,
                MockNmaasUserDetailsComponent,
                MockNmaasUserPrivilegesComponent,
                MockNmaasSshKeysComponent,
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
                {provide: ProfileService, useValue: profileServiceSpy},
                {provide: ContentDisplayService, useValue: {}},
                {provide: InternationalizationService, useValue: internationalizationSpy}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ProfileComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
