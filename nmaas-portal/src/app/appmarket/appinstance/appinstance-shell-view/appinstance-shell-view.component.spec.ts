import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppInstanceShellViewComponent} from './appinstance-shell-view.component';
import {SshShellComponent} from '../ssh-shell/ssh-shell.component';
import {NgTerminalModule} from 'ng-terminal';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {Component} from '@angular/core';
import {ModalComponent} from '../../../shared/modal';

@Component({
    selector: 'nmaas-modal',
    template: '<p>Nmaas Modal Mock</p>'
})
class NmaasModalMockComponent extends ModalComponent {
}

describe('AppInstanceShellViewComponent', () => {
    let component: AppInstanceShellViewComponent;
    let fixture: ComponentFixture<AppInstanceShellViewComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                AppInstanceShellViewComponent,
                SshShellComponent,
                NmaasModalMockComponent
            ],
            imports: [
                NgTerminalModule,
                HttpClientTestingModule,
                RouterTestingModule,
                TranslateModule.forRoot({
                  loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                  }
                })
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppInstanceShellViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
