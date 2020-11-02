import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppVersionCreateWizardComponent} from './appversioncreatewizard.component';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import createSpyObj = jasmine.createSpyObj;
import {AppImagesService, AppsService, TagService} from '../../../service';
import {ConfigTemplateService} from '../../../service/configtemplate.service';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {StepsModule} from 'primeng/steps';
import {SharedModule} from '../../../shared';

describe('AppVersionCreateWizardComponent', () => {
    let component: AppVersionCreateWizardComponent;
    let fixture: ComponentFixture<AppVersionCreateWizardComponent>;

    beforeEach(async(() => {
        const tagServiceSpy = createSpyObj('TagService', ['getTags'])
        tagServiceSpy.getTags.and.returnValue(of([]))

        const configTemplateServiceSpy = createSpyObj('ConfigTemplateService', ['getConfigTemplate'])

        TestBed.configureTestingModule({
            declarations: [AppVersionCreateWizardComponent],
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
                {provide: ConfigTemplateService, useValue: configTemplateServiceSpy},
                {provide: AppImagesService, useValue: {}}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppVersionCreateWizardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
