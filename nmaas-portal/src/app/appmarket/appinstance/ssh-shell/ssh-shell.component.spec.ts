import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SshShellComponent} from './ssh-shell.component';
import {NgTerminalModule} from 'ng-terminal';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {Component} from '@angular/core';
import {ModalComponent} from '../../../shared/modal';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RouterTestingModule} from '@angular/router/testing';
import createSpyObj = jasmine.createSpyObj;
import {ShellClientService} from '../../../service/shell-client.service';
import {concat, of, throwError} from 'rxjs';

@Component({
    selector: 'nmaas-modal',
    template: '<p>Nmaas Modal Mock</p>'
})
class NmaasModalMockComponent extends ModalComponent {
}

describe('SshShellComponent', () => {
    let component: SshShellComponent;
    let fixture: ComponentFixture<SshShellComponent>;

    const shellClientServiceSpy = createSpyObj('ShellClientService',
        ['getServerSentEvent', 'initConnection', 'closeConnection'])

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
            ],
            providers: [
                {provide: ShellClientService, useValue: shellClientServiceSpy}
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

    it('should attempt connection', () => {
        component.appInstanceId = 1;

        shellClientServiceSpy.initConnection.and.returnValue(of('session-identifier'));
        shellClientServiceSpy.getServerSentEvent.and.returnValue(of());

        component.ngOnInit();

        expect(shellClientServiceSpy.initConnection).toHaveBeenCalled();
        expect(shellClientServiceSpy.getServerSentEvent).toHaveBeenCalled();
    });

    it('should not connect on connection error', () => {
        component.appInstanceId = 1;

        shellClientServiceSpy.initConnection.and.returnValue(throwError('Test error'));

        component.ngOnInit();

        expect(shellClientServiceSpy.initConnection).toHaveBeenCalled();
        // expect(shellClientServiceSpy.getServerSentEvent).toHaveBeenCalledTimes(0);
    });

    it('should disconnect with modal', () => {
        component.appInstanceId = 1;

        shellClientServiceSpy.initConnection.and.returnValue(of('session-identifier'));
        shellClientServiceSpy.getServerSentEvent.and.returnValue(of());

        component.ngOnInit();

        expect(shellClientServiceSpy.initConnection).toHaveBeenCalled();

        component.disconnectWithModal()
    })

    it('should parse messages', () => {
        component.appInstanceId = 1;

        shellClientServiceSpy.initConnection.and.returnValue(of('session-identifier'));
        shellClientServiceSpy.getServerSentEvent.and.returnValue(concat(of(
            {data: null}, {data: ''}, {data: 'message with <#>NEWLINE<#>'}
            ),
            throwError('Test error')
        ));

        component.ngOnInit();

        expect(shellClientServiceSpy.initConnection).toHaveBeenCalled();
        expect(shellClientServiceSpy.getServerSentEvent).toHaveBeenCalled();
    })
});
