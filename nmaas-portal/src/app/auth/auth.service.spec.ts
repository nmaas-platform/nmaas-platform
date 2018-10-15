/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AuthService } from './auth.service';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "../service";
import {JWT_OPTIONS, JwtHelperService} from "@auth0/angular-jwt";
import {InjectionToken} from "@angular/core";

describe('Service: Auth', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthService, HttpClient, HttpHandler, AppConfigService, JwtHelperService, InjectionToken, JWT_OPTIONS]
    });
  });

 // it('should ...', inject([""], (service: AuthService) => {
 //   expect(service).toBeTruthy();
  //}));
});
