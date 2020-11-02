import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DomainFilterComponent} from './domainfilter.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import createSpyObj = jasmine.createSpyObj;
import {DomainService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {UserDataService} from '../../../service/userdata.service';
import {of} from 'rxjs';
import {Domain} from '../../../model/domain';

describe('DomainFilterComponent', () => {
    let component: DomainFilterComponent;
    let fixture: ComponentFixture<DomainFilterComponent>;

    const domainG: Domain = {
        id: 1,
        name: 'global',
        codename: 'global',
        active: true,
        domainDcnDetails: undefined,
        domainTechDetails: undefined,
        applicationStatePerDomain: []
    }

    const domain1: Domain = {
        id: 2,
        name: 'domain one',
        codename: 'dom-1',
        active: true,
        domainDcnDetails: undefined,
        domainTechDetails: undefined,
        applicationStatePerDomain: []
    };

    const domain2: Domain = {
        id: 3,
        name: 'domain two',
        codename: 'dom-2',
        active: true,
        domainDcnDetails: undefined,
        domainTechDetails: undefined,
        applicationStatePerDomain: []
    };

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole']);
        authServiceSpy.hasRole.and.returnValue(true)

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId', 'getAll', 'getMyDomains'])
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)
        domainServiceSpy.getAll.and.returnValue(of([domainG, domain1, domain2]))
        domainServiceSpy.getMyDomains.and.returnValue(of([domain1, domain2]))

        TestBed.configureTestingModule({
            declarations: [DomainFilterComponent],
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
                {provide: AuthService, useValue: authServiceSpy},
                {
                    provide: UserDataService, useValue: {
                        selectedDomainId: of(1),
                        selectDomainId: function(data) {console.log('selectDomainId fake', data)},
                    }
                },
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(DomainFilterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
