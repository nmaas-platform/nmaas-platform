import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NewSshKeyComponent} from './new-ssh-key.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import createSpyObj = jasmine.createSpyObj;
import {SSHKeyService} from '../../../service/sshkey.service';
import {of} from 'rxjs';
import {SharedModule} from '../../shared.module';

describe('NewSshKeyComponent', () => {
    let component: NewSshKeyComponent;
    let fixture: ComponentFixture<NewSshKeyComponent>;

    let sshKeyServiceSpy = undefined;

    beforeEach(async(() => {
        sshKeyServiceSpy = createSpyObj('SSHKeyService', ['createKey'])

        TestBed.configureTestingModule({
            declarations: [NewSshKeyComponent],
            imports: [
                RouterTestingModule,
                ReactiveFormsModule,
                SharedModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })
            ],
            providers: [
                {provide: SSHKeyService, useValue: sshKeyServiceSpy}
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NewSshKeyComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should create new key', () => {
        sshKeyServiceSpy.createKey.and.returnValue(of({}))

        component.create()

        expect(sshKeyServiceSpy.createKey).toHaveBeenCalledTimes(1)
    })
});
