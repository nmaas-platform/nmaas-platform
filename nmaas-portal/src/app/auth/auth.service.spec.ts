/* tslint:disable:no-unused-variable */
import {TestBed, async} from '@angular/core/testing';
import {AuthService} from './auth.service';
import {AppConfigService} from "../service";
import {JwtHelperService} from "@auth0/angular-jwt";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {Role} from "../model/userrole";

describe('Service: Auth', () => {
    let authService: AuthService;
    let appConfigServiceSpy: jasmine.SpyObj<AppConfigService>;
    let jwtHelperServiceSpy: jasmine.SpyObj<JwtHelperService>;

    let store: any = {};

    beforeEach(async(() => {
        let appConfigServiceStub: Partial<AppConfigService> = {
            config: {
                apiUrl: 'http://api.url',
                tokenName: 'token',
            }
        };

        let jwtSpy = jasmine.createSpyObj('JwtHelperService', ['decodeToken', 'isTokenExpired']);
        jwtSpy.decodeToken.and.returnValue({
            language: 'pl',
            sub: 'test-user',
            scopes: [{authority: '1:'+Role[Role.ROLE_SYSTEM_ADMIN]}, {authority: '2:'+Role[Role.ROLE_USER]}]
        });
        jwtSpy.isTokenExpired.and.callFake((arg: string): boolean => {
            return arg !== 'valid';
        });

        TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule
            ],
            providers: [
                AuthService,
                {provide: AppConfigService, useValue: appConfigServiceStub},
                {provide: JwtHelperService, useValue: jwtSpy},
            ],
        });

        authService = TestBed.get(AuthService);
        appConfigServiceSpy = TestBed.get(AppConfigService);
        jwtHelperServiceSpy = TestBed.get(JwtHelperService);
        // spyOn(appConfigServiceSpy, 'getTestInstanceModalKey').and.returnValue("test-instance-modal-key");

        // local store mock
        store = {token: 'valid'};

        spyOn(localStorage, 'getItem').and.callFake(function (key) {
            return store[key];
        });
        spyOn(localStorage, 'setItem').and.callFake(function (key, value) {
            return store[key] = value + '';
        });
        spyOn(localStorage, 'removeItem').and.callFake(function (key) {
            delete store[key];
        });

    }));

    it('should create service', () => {
        expect(authService).toBeTruthy();
    });

    it('should get language from local store', () => {
        store = {lang: 'en'};
        const result = authService.getSelectedLanguage();
        expect(result).toEqual('en');
    });

    it('should get language from token', () => {
        const result = authService.getSelectedLanguage();
        expect(result).toEqual('pl');
    });

    it('should get username from token', () => {
        const result = authService.getUsername();
        expect(result).toEqual('test-user');
        store = {};
        expect(authService.getUsername()).toEqual(null)
    });

    it('should return true when role is present or false when role is absent', () => {
        const result = authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN]);
        expect(result).toEqual(true);
        const result2 = authService.hasRole(Role[Role.ROLE_DOMAIN_ADMIN]);
        expect(result2).toEqual(false);
    });

    it('should return true when role is present in domain and false otherwise', () => {
        const result = authService.hasDomainRole(2,Role[Role.ROLE_USER]);
        expect(result).toEqual(true);
        const result2 = authService.hasDomainRole(2,Role[Role.ROLE_DOMAIN_ADMIN]);
        expect(result2).toEqual(false);
    });

    it('should return domains from roles', () => {
        const result  = authService.getDomains();
        expect(result).toContain(1);
        expect(result).toContain(2);
        store = {token: null};
        expect(authService.getDomains().length).toEqual(0)
    });

    it('should get roles', () => {
        const result = authService.getRoles();
        expect(result).toContain(Role[Role.ROLE_SYSTEM_ADMIN]);
        expect(result).toContain(Role[Role.ROLE_USER]);
        store = {token: null};
        expect(authService.getRoles().length).toEqual(0)
    });

    it('should return roles in domain map', () => {
        const result = authService.getDomainRoles();
        expect(result.size).toEqual(2);
        expect(result.has(1)).toEqual(true);
        expect(result.has(2)).toEqual(true);
        expect(result.get(1).getRoles()).toContain(Role[Role.ROLE_SYSTEM_ADMIN]);
        expect(result.get(2).hasRole(Role[Role.ROLE_USER])).toEqual(true);
        store = {token: null};
        expect(authService.getDomainRoles().size).toEqual(0);
    });

    it('should get domain where role present', () => {
        const r1 = authService.getDomainsWithRole(Role[Role.ROLE_SYSTEM_ADMIN]);
        expect(r1.length).toEqual(1);
        expect(r1).toContain(1);
        const r2 = authService.getDomainsWithRole(Role[Role.ROLE_USER]);
        expect(r2.length).toEqual(1);
        expect(r2).toContain(2);
        const r3 = authService.getDomainsWithRole(Role[Role.ROLE_DOMAIN_ADMIN]);
        expect(r3.length).toEqual(0);
    });

    it('should remove token on logout', () => {
        authService.logout();
        expect(store['token']).not.toBeDefined();
    });

    it('should be logged in when token is present and valid', () => {
        store = {token: 'valid'};
        let r: boolean;
        r = authService.isLogged();
        expect(r).toEqual(true);
        store = {token: 'expired'};
        r = authService.isLogged();
        expect(r).toEqual(false);
        store = {token: null};
        r = authService.isLogged();
        expect(r).toEqual(false);
    });

});
