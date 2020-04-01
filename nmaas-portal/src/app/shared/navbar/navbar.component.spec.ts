import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NavbarComponent} from './navbar.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {of} from "rxjs";
import {RouterTestingModule} from "@angular/router/testing";
import {AuthService} from "../../auth/auth.service";
import {DomainService} from "../../service";
import {Component, Directive, Input} from "@angular/core";
import {InternationalizationService} from "../../service/internationalization.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

@Component({
    selector: 'nmaas-domain-filter',
    template: '<p>Mock nmaas-domain-filter Component</p>'
})
class MockDomainFilter {
}

@Component({
    selector: 'app-modal-notification-send',
    template: '<p>Mock modal</p>'
})
class MockNotificationSendModalComponent {
}

@Directive({
    selector: '[roles]',
    inputs: ['roles']
})
class MockRolesDirective {
   @Input() set roles(allowedRoles: Array<string>) {}
}

describe('NavbarComponent_Shared', () => {
    let component: NavbarComponent;
    let fixture: ComponentFixture<NavbarComponent>;

    let languageService: InternationalizationService;
    let domainService: DomainService;
    let authService: AuthService;

    beforeEach(async(() => {
        let mockDomainService = jasmine.createSpyObj(['getGlobalDomainId']);
        mockDomainService.getGlobalDomainId.and.returnValue(1);
        let mockLanguageService = jasmine.createSpyObj(['getEnabledLanguages', 'shouldUpdate']);
        mockLanguageService.getEnabledLanguages.and.returnValue(of(['en', 'fr', 'pl']));
        mockLanguageService.shouldUpdate.and.returnValue(false);
        let mockAuthService = jasmine.createSpyObj(['isLogged', 'hasRole', 'getDomains', 'getRoles']);
        mockAuthService.isLogged.and.returnValue(false);
        mockAuthService.hasRole.and.returnValue(false);
        mockAuthService.getDomains.and.returnValue([]);
        mockAuthService.getRoles.and.returnValue([]);

        TestBed.configureTestingModule({
            declarations: [
                NavbarComponent,
                MockRolesDirective,
                MockDomainFilter,
                MockNotificationSendModalComponent,
            ],
            imports: [
                HttpClientTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
                RouterTestingModule,
            ],
            providers: [
                {provide: DomainService, useValue: mockDomainService},
                {provide: InternationalizationService, useValue: mockLanguageService},
                {provide: AuthService, useValue: mockAuthService},

            ]
        }).compileComponents().then((result) => {
          console.log(result);
        });
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NavbarComponent);
        component = fixture.componentInstance;
        languageService = fixture.debugElement.injector.get(InternationalizationService);
        domainService = fixture.debugElement.injector.get(DomainService);
        authService = fixture.debugElement.injector.get(AuthService);
        // future use
        // spyOn(languageService, 'getEnabledLanguages').and.returnValue(of(['en', 'fr', 'pl']));
        // spyOn(languageService, 'shouldUpdate').and.returnValue(false);
        // spyOn(domainService, 'getGlobalDomainId').and.returnValue(1);
        // spyOn(authService, 'isLogged').and.returnValue(false);
        // spyOn(authService, 'hasRole').and.returnValue(false);
        // spyOn(authService, 'getDomains').and.returnValue([]);
        // spyOn(authService, 'getRoles').and.returnValue([]);
        component.useLanguage('en');
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should change language', () => {
        component.useLanguage("fr");
        expect(component.getCurrent()).toBe("fr");
    })
});
