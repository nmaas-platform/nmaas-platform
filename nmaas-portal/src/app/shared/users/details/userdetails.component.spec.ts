import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UserDetailsComponent} from './userdetails.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AuthService} from '../../../auth/auth.service';
import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import createSpyObj = jasmine.createSpyObj;
import {DomainService} from '../../../service';
import {of} from 'rxjs';

@Component({
    selector: 'nmaas-password',
    template: '<p>Mock Password Component</p>',
})
export class MockPasswordComponent {}

describe('UserDetailsComponent', () => {
    let component: UserDetailsComponent;
    let fixture: ComponentFixture<UserDetailsComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['getUsername'])
        authServiceSpy.getUsername.and.returnValue('andrew')

        TestBed.configureTestingModule({
            declarations: [UserDetailsComponent, MockPasswordComponent],
            imports: [
                RouterTestingModule,
                FormsModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
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
