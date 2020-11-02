import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppInstanceListComponent} from './appinstancelist.component';
import {FormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppConfigService, AppImagesService, AppInstanceService, DomainService, UserService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {UserDataService} from '../../../service/userdata.service';
import {of} from 'rxjs';
import createSpyObj = jasmine.createSpyObj;
import {SessionService} from '../../../service/session.service';
import {PipesModule} from '../../../pipe/pipes.module';
import {SortService} from '../../../service/sort.service';
import {NgxPaginationModule} from 'ngx-pagination';
import {TranslateStateModule} from '../../../shared/translate-state/translate-state.module';
import {RolesDirective} from '../../../directive/roles.directive';
import {JwtModule} from '@auth0/angular-jwt';
import {Pipe, PipeTransform} from '@angular/core';
import {HttpClientTestingModule} from '@angular/common/http/testing';

@Pipe({ name: 'keys'})
class KeysPipe implements PipeTransform {

    transform(value: any, args?: any): any {
        const keys = [];
        for (const enumMember in value) {
            if (parseInt(enumMember, 10) >= 0) {
                keys.push({key: enumMember, value: value[enumMember]});
                console.log('enum member: ', value[enumMember]);
            }
        }
        return keys;
    }

}

describe('AppInstanceListComponent', () => {
    let component: AppInstanceListComponent;
    let fixture: ComponentFixture<AppInstanceListComponent>;

    beforeEach(async(() => {
        const authServiceSpy = createSpyObj('AuthService', ['hasRole', 'hasDomainRole']);
        authServiceSpy.hasRole.and.returnValue(true)
        authServiceSpy.hasDomainRole.and.returnValue(true)

        const domainServiceSpy = createSpyObj('DomainService', ['getGlobalDomainId', 'getAll'])
        domainServiceSpy.getGlobalDomainId.and.returnValue(1)
        domainServiceSpy.getAll.and.returnValue(of([]))

        const appConfigSpy = createSpyObj('AppConfigService', ['getApiUrl', 'getHttpTimeout', 'getNmaasGlobalDomainId'])
        appConfigSpy.getApiUrl.and.returnValue('mock.url')
        appConfigSpy.getHttpTimeout.and.returnValue(10000)
        appConfigSpy.getNmaasGlobalDomainId.and.returnValue(1)

        const sessionServiceSpy = createSpyObj('SessionService', ['registerCulture'])

        TestBed.configureTestingModule({
            declarations: [
                AppInstanceListComponent,
                KeysPipe
            ],
            imports: [
                FormsModule,
                RouterTestingModule,
                NgxPaginationModule,
                HttpClientTestingModule,
                JwtModule.forRoot({}),
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: AppInstanceService, useValue: {}},
                {provide: DomainService, useValue: domainServiceSpy},
                {provide: AppConfigService, useValue: appConfigSpy},
                {provide: AuthService, useValue: authServiceSpy},
                {
                    provide: UserDataService,
                    useValue: {
                        selectedDomainId: of(1)
                    }
                },
                {provide: SessionService, useValue: sessionServiceSpy},
                {provide: SortService, useValue: {}},
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AppInstanceListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
