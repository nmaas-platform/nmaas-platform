import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SelectPodModalComponent} from './select-pod-modal.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../../shared';

describe('SelectPodModalComponent', () => {
    let component: SelectPodModalComponent;
    let fixture: ComponentFixture<SelectPodModalComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SelectPodModalComponent],
            imports: [
                SharedModule,
                BrowserAnimationsModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelectPodModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
