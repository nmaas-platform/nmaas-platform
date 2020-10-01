import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppCreateWizardComponent} from './app-create-wizard.component';
import {AppImagesService, AppsService, TagService} from '../../../service';
import {RouterTestingModule} from '@angular/router/testing';
import {InternationalizationService} from '../../../service/internationalization.service';
import {ConfigTemplateService} from '../../../service/configtemplate.service';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {StepsModule} from 'primeng/steps';
import {SharedModule} from '../../../shared';
import {of} from 'rxjs';
import createSpyObj = jasmine.createSpyObj;

describe('AppCreateWizardComponent', () => {
    let component: AppCreateWizardComponent;
    let fixture: ComponentFixture<AppCreateWizardComponent>;

    let appsService: AppsService = undefined;
    let appImagesService = undefined;

    beforeEach(async(() => {
        const tagServiceSpy = createSpyObj('TagService', ['getTags'])
        tagServiceSpy.getTags.and.returnValue(of([]))

        const internationalizationSpy = createSpyObj('InternationalizationService', ['getAllSupportedLanguages'])
        internationalizationSpy.getAllSupportedLanguages.and.returnValue(of([
            { language: 'en', enabled: true, content: {} },
            { language: 'en', enabled: true, content: {} },
        ]))

        const configTemplateServiceSpy = createSpyObj('ConfigTemplateService', ['getConfigTemplate'])

        TestBed.configureTestingModule({
            declarations: [AppCreateWizardComponent],
            imports: [
                StepsModule,
                SharedModule,
                RouterTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: TagService, useValue: tagServiceSpy},
                {provide: AppsService, useValue: {}},
                {provide: InternationalizationService, useValue: internationalizationSpy},
                {provide: ConfigTemplateService, useValue: configTemplateServiceSpy},
                {provide: AppImagesService, useValue: {}}
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppCreateWizardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        appsService = TestBed.inject(AppsService)
        appImagesService = TestBed.inject(AppImagesService)
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

