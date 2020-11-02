import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ConfigurationDetailsComponent} from './configurationdetails.component';
import {FormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {ConfigurationService} from '../../../../service';
import {of} from 'rxjs';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {InternationalizationService} from '../../../../service/internationalization.service';
import createSpyObj = jasmine.createSpyObj;

describe('ConfigurationDetailsComponent', () => {
    let component: ConfigurationDetailsComponent;
    let fixture: ComponentFixture<ConfigurationDetailsComponent>;

    beforeEach(async(() => {
        const internationalizationSpy = createSpyObj('InternationalizationService', ['getEnabledLanguages', 'getAllSupportedLanguages'])
        internationalizationSpy.getAllSupportedLanguages.and.returnValue(of([]))
        internationalizationSpy.getEnabledLanguages.and.returnValue(of(['en', 'pl']))

        const configurationServiceSpy = createSpyObj('ConfigurationService', ['getConfiguration', 'updateConfiguration'])
        configurationServiceSpy.getConfiguration.and.returnValue(of())
        configurationServiceSpy.updateConfiguration.and.returnValue(of())

        TestBed.configureTestingModule({
            declarations: [ConfigurationDetailsComponent],
            imports: [
                FormsModule,
                RouterTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: ConfigurationService, useValue: configurationServiceSpy},
                {provide: InternationalizationService, useValue: internationalizationSpy}
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ConfigurationDetailsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
