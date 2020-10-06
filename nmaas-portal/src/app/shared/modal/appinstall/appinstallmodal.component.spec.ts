import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppInstallModalComponent} from './appinstallmodal.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppInstanceService, DomainService} from '../../../service';
import {UserDataService} from '../../../service/userdata.service';
import {of} from 'rxjs';
import {SharedModule} from '../../shared.module';
import {ApplicationBase} from '../../../model/application-base';
import {ApplicationState} from '../../../model/application-state';
import {Rate} from '../../../model';
import {Domain} from '../../../model/domain';

describe('AppInstallmodalComponent', () => {
    let component: AppInstallModalComponent;
    let fixture: ComponentFixture<AppInstallModalComponent>;

    const appBase: ApplicationBase = {
        id: 1,
        name: 'app',
        license: undefined,
        licenseUrl: undefined,
        wwwUrl: undefined,
        sourceUrl: undefined,
        issuesUrl: undefined,
        nmaasDocumentationUrl: undefined,
        descriptions: [],
        tags: [],
        versions: [
            {id: 1, version: '1.0.0', state: ApplicationState.ACTIVE, appVersionId: 1},
            {id: 2, version: '1.0.1', state: ApplicationState.ACTIVE, appVersionId: 2},
            {id: 3, version: '1.0.3', state: ApplicationState.NEW, appVersionId: 3}
        ],
        rate: new Rate(4, 4.5, new Map([[5, 1], [4, 1]]))
    }

    const domain: Domain = {
        id: 1,
        name: 'domain one',
        codename: 'dom-1',
        active: true,
        domainDcnDetails: undefined,
        domainTechDetails: undefined,
        applicationStatePerDomain: []
    }

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [AppInstallModalComponent],
            imports: [
                RouterTestingModule,
                SharedModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: DomainService, useValue: {}},
                {provide: AppInstanceService, useValue: {}},
                {
                    provide: UserDataService, useValue: {
                        selectedDomainId: of(1)
                    }
                }
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppInstallModalComponent);
        component = fixture.componentInstance;
        component.app = appBase;
        component.domain = domain;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy()
    })

});
