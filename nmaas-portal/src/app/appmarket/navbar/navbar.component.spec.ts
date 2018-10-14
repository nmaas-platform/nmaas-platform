/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { NavbarComponent } from './navbar.component';
import {AuthService, DomainRoles} from "../../auth/auth.service";
import {AppConfigService, DomainService} from "../../service";
import {RouterTestingModule} from "@angular/router/testing";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {JwtHelperService} from "@auth0/angular-jwt";
import {Authority} from "../../model";
import {isUndefined} from "util";
import {Observable} from "rxjs";

class MockAuthService{

    public loginUsingSsoService:boolean;

    constructor() {}

    private storeToken(token: string): void {

    }

    private getToken(): string {
        return String("");
    }

    private removeToken(): void {

    }

    public getUsername(): string {
      return String("admin")
    }

    public hasRole(name: string): boolean {
        return false;
    }

    public hasDomainRole(domainId: number, name: string): boolean {
        return false;
    }

    public getDomainRoles(): Map<number, DomainRoles> {
        return new Map<number, DomainRoles>();
    }

    public getRoles(): string[] {
        const roles: string[] = [];
        return roles;
    }


    public getDomains(): number[] {
        const domains: number[] = [];
        return domains;
    }

    public getDomainsWithRole(name: string): number[] {
        const domainsWithRole: number[] = [];
        return domainsWithRole;
    }

    public login(username: string, password: string): Observable<boolean> {
      return Observable.of<boolean>();
    }

    public propagateSSOLogin(userid: string): Observable<boolean> {
        return Observable.of<boolean>();
    }

    public logout(): void {
        this.removeToken();
    }

    public isLogged(): boolean {
      return true;
    }
}

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NavbarComponent ],
        imports:[
          RouterTestingModule,

        ],
        providers:[
            {provide: AuthService, useClass: MockAuthService},
            DomainService,
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
