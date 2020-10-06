import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RouterTestingModule} from '@angular/router/testing';
import {ScreenshotsComponent} from './screenshots.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppsService} from '../../service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {PipesModule} from '../../pipe/pipes.module';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

// TODO mock secure pipe

describe('ScreenshotsComponent', () => {
    let component: ScreenshotsComponent;
    let fixture: ComponentFixture<ScreenshotsComponent>;

    beforeEach(async(() => {
        const appsServiceSpy = createSpyObj('AppsService', ['getAppScreenshotsByUrl'])
        appsServiceSpy.getAppScreenshotsByUrl.and.returnValue(of([]))

        TestBed.configureTestingModule({
            declarations: [
                ScreenshotsComponent,
            ],
            imports: [
                RouterTestingModule,
                HttpClientTestingModule,
                PipesModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
        providers: [
            {provide: AppsService, useValue: appsServiceSpy}
        ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ScreenshotsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
})
