import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RouterTestingModule} from '@angular/router/testing';
import {RateComponent} from './rate.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppsService} from '../../service';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';
import {Rate} from '../../model';


describe('RateComponent', () => {
    let component: RateComponent;
    let fixture: ComponentFixture<RateComponent>;

    beforeEach(async(() => {
        const appsServiceSpy = createSpyObj('AppsService', ['getAppRateByUrl'])
        appsServiceSpy.getAppRateByUrl.and.returnValue(of(new Rate(4, 4.5, new Map([[5, 1], [4, 1]]))))

        TestBed.configureTestingModule({
            declarations: [
                RateComponent,
            ],
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
                {provide: AppsService, useValue: appsServiceSpy}
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
})
