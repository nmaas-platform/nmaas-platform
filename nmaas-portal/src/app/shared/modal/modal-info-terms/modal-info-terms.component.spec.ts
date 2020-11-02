import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModalInfoTermsComponent} from './modal-info-terms.component';
import {ModalComponent} from '../modal.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ContentDisplayService} from '../../../service/content-display.service';
import {of} from 'rxjs';
import createSpyObj = jasmine.createSpyObj;

describe('ModalInfoTermsComponent', () => {
    let component: ModalInfoTermsComponent;
    let fixture: ComponentFixture<ModalInfoTermsComponent>;

    beforeEach(async(() => {
        const contentDisplayServiceSpy = createSpyObj('ContentDisplayService', ['getContent'])
        contentDisplayServiceSpy.getContent.and.returnValue(of({}))

        TestBed.configureTestingModule({
            declarations: [ModalInfoTermsComponent, ModalComponent],
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
                {provide: ContentDisplayService, useValue: contentDisplayServiceSpy}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ModalInfoTermsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
