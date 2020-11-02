import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppPreviewComponent} from './apppreview.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppImagesService, AppsService} from '../../../service';

describe('AppPreviewComponent', () => {
    let component: AppPreviewComponent;
    let fixture: ComponentFixture<AppPreviewComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [AppPreviewComponent],
            imports: [
                RouterTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })
            ],
            providers: [
                {provide: AppsService, useValue: {}},
                {provide: AppImagesService, useValue: {}},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppPreviewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy()
    })

});
