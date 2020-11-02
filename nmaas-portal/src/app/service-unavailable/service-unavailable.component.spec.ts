import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ServiceUnavailableComponent} from './service-unavailable.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {MonitorService} from '../service/monitor.service';
import {InternationalizationService} from '../service/internationalization.service';
import {ServiceUnavailableService} from './service-unavailable.service';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('ServiceUnavailableComponent', () => {
    let component: ServiceUnavailableComponent;
    let fixture: ComponentFixture<ServiceUnavailableComponent>;

    beforeEach(async(() => {
        const internationalizationServiceSpy = createSpyObj('InternationalizationService', ['getEnabledLanguages'])
        internationalizationServiceSpy.getEnabledLanguages.and.returnValue(of(['en', 'pl']))

        TestBed.configureTestingModule({
            declarations: [ServiceUnavailableComponent],
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
                {provide: MonitorService, useValue: {}},
                {provide: InternationalizationService, useValue: internationalizationServiceSpy},
                {provide: ServiceUnavailableService, useValue: {}}
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ServiceUnavailableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    // TODO fix component not to use document directly, or find a way to mock document.getElementById
    xit('should create', () => {
        expect(component).toBeTruthy();
    });
});
