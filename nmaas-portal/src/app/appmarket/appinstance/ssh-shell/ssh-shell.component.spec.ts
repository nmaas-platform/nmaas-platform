import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SshShellComponent} from './ssh-shell.component';
import {NgTerminalModule} from 'ng-terminal';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {Component} from '@angular/core';
import {ModalComponent} from '../../../shared/modal';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RouterTestingModule} from '@angular/router/testing';

@Component({
    selector: 'nmaas-modal',
    template: '<p>Nmaas Modal Mock</p>'
})
class NmaasModalMockComponent extends ModalComponent {
}

describe('SshShellComponent', () => {
    let component: SshShellComponent;
    let fixture: ComponentFixture<SshShellComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                SshShellComponent,
                NmaasModalMockComponent,
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
        fixture = TestBed.createComponent(SshShellComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
