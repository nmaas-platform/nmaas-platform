import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AboutComponent} from './about.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppConfigService, ChangelogService} from '../../service';
import {RouterTestingModule} from '@angular/router/testing';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';
import {Component} from '@angular/core';

@Component({
    selector: 'app-contact',
    template: '<p>App Contact Component Mock</p>'
})
class MockAppContactComponent {
}

@Component({
    selector: 'app-changelog',
    template: '<p>App Changelog Component Mock</p>'
})
class MockChangelogComponent {
}

describe('AboutComponent', () => {
    let component: AboutComponent;
    let fixture: ComponentFixture<AboutComponent>;

    const appConfigServiceSpy = createSpyObj('AppConfigService', ['getShowGitInfo'])
    appConfigServiceSpy.getShowGitInfo.and.returnValue(true);

    const changelogServiceSpy = createSpyObj('ChangelogService', ['getGitInfo'])
    changelogServiceSpy.getGitInfo.and.returnValue(of({}))

    beforeEach(async () => {
        await TestBed.configureTestingModule({
                declarations: [AboutComponent, MockChangelogComponent, MockAppContactComponent],
                imports: [
                    TranslateModule.forRoot({
                        loader: {
                            provide: TranslateLoader,
                            useClass: TranslateFakeLoader
                        }
                    }),
                    RouterTestingModule,
                ],
                providers: [
                    {provide: ChangelogService, useValue: changelogServiceSpy},
                    {provide: AppConfigService, useValue: appConfigServiceSpy},
                ]
            }
        ).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AboutComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
