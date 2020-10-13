import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LanguageListComponent} from './languagelist.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {FormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {InternationalizationService} from '../../../../service/internationalization.service';
import {AppConfigService} from '../../../../service';
import {of} from 'rxjs';
import {ModalComponent} from '../../../../shared/modal';

describe('LanguagelistComponent', () => {
    let component: LanguageListComponent;
    let fixture: ComponentFixture<LanguageListComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [LanguageListComponent, ModalComponent],
            imports: [
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
                FormsModule,
                RouterTestingModule
            ],
            providers: [
                {
                    provide: InternationalizationService, useValue: {
                        getAllSupportedLanguages: function () {
                            return of([])
                        }
                    }
                },
                {provide: AppConfigService, useValue: {}}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LanguageListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy()
    });

});
