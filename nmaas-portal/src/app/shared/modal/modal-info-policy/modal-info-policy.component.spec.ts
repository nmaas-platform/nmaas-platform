import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ModalInfoPolicyComponent} from './modal-info-policy.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ModalComponent} from '../modal.component';
import {ContentDisplayService} from '../../../service/content-display.service';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('ModalInfoPolicyComponent', () => {
    let component: ModalInfoPolicyComponent;
    let fixture: ComponentFixture<ModalInfoPolicyComponent>;

    beforeEach(async(() => {
        const contentDisplayServiceSpy = createSpyObj('ContentDisplayService', ['getContent'])
        contentDisplayServiceSpy.getContent.and.returnValue(of({}))

        TestBed.configureTestingModule({
            declarations: [ModalInfoPolicyComponent, ModalComponent],
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
        fixture = TestBed.createComponent(ModalInfoPolicyComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
