import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RatingExtendedComponent} from './rating-extended.component';
import {AppsService} from '../../service';
import {Observable, of} from 'rxjs';
import {Rate} from '../../model';
import {TooltipModule} from 'ng2-tooltip-directive';
import {HttpResponse} from '@angular/common/http';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';

class MockAppService {
    public getAppRateByUrl(urlPath: string): Observable<Rate> {
        return of(new Rate(1, 1, new Map()));
    }

    public setMyAppRateByUrl(urlPath: string): Observable<any> {
        return of(HttpResponse.prototype);
    }
}

describe('RatingExtendedComponent', () => {
    let component: RatingExtendedComponent;
    let fixture: ComponentFixture<RatingExtendedComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [RatingExtendedComponent],
            imports: [
                TooltipModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: AppsService, useClass: MockAppService},
            ]
        });
        TestBed.compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RatingExtendedComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
