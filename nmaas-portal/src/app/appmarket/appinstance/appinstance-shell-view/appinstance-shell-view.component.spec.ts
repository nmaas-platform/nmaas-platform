import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppInstanceShellViewComponent} from './appinstance-shell-view.component';
import {SshShellComponent} from '../ssh-shell/ssh-shell.component';
import {NgTerminalModule} from 'ng-terminal';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';

describe('AppInstanceShellViewComponent', () => {
    let component: AppInstanceShellViewComponent;
    let fixture: ComponentFixture<AppInstanceShellViewComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                AppInstanceShellViewComponent,
                SshShellComponent
            ],
            imports: [
                NgTerminalModule,
                HttpClientTestingModule,
                RouterTestingModule,
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
