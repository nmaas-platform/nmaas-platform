import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ChangelogComponent} from './changelog.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {ChangelogService} from '../../service';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('ChangelogComponent', () => {
    let component: ChangelogComponent;
    let fixture: ComponentFixture<ChangelogComponent>;

    const changelogServiceSpy = createSpyObj('ChangelogService', ['getChangelog'])
    changelogServiceSpy.getChangelog.and.returnValue(of({}))

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ChangelogComponent],
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
                {provide: ChangelogService, useValue: changelogServiceSpy}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ChangelogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
