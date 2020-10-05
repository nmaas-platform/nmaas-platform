import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DomainsListComponent} from './domainslist.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import createSpyObj = jasmine.createSpyObj;
import {AuthService} from '../../../auth/auth.service';
import {DomainService} from '../../../service';
import {of} from 'rxjs';

describe('DomainslistComponent', () => {
    let component: DomainsListComponent;
    let fixture: ComponentFixture<DomainsListComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId', 'getAll'])
        domainServiceSpy.getAll.and.returnValue(of([]))
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)

        TestBed.configureTestingModule({
            declarations: [DomainsListComponent],
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
                {provide: AuthService, useValue: authServiceSpy},
                {provide: DomainService, useValue: domainServiceSpy},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(DomainsListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
