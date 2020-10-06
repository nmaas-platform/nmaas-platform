import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {WelcomeComponent} from './welcome.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppConfigService} from '../service';
import {ServiceUnavailableService} from '../service-unavailable/service-unavailable.service';

describe('WelcomeComponent', () => {
    let component: WelcomeComponent;
    let fixture: ComponentFixture<WelcomeComponent>;

    const dummyElement = document.createElement('div');
    // const dummyElement = {
    //     style: {},
    //     offsetHeight: 10
    // };

    beforeEach(async(() => {
        // const documentSpy = createSpyObj('document', ['getElementById'])
        // documentSpy.getElementById.and.returnValue({
        //     style: {},
        //     offsetHeight: 10
        // })

        TestBed.configureTestingModule({
            declarations: [WelcomeComponent],
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
                {provide: AppConfigService, useValue: {}},
                {
                    provide: ServiceUnavailableService, useValue: {
                        isServiceAvailable: true
                    }
                },
                // {provide: document, useValue: documentSpy}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(WelcomeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        spyOn(document, 'getElementById').and.returnValue(dummyElement)

        // document.getElementById = jasmine.createSpy('HTML Element').and.returnValue(dummyElement);
    });

    // TODO - find a way to mock 'document'

    // it('document should always return dummy element', () => {
    //     const result = document.getElementById('any')
    //     expect(result).toBeTruthy()
    //     expect(result).toEqual(dummyElement)
    // })
    //
    // it('should create', () => {
    //     expect(component).toBeTruthy();
    // });
});
