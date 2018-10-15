/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AppsService } from './apps.service';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";

describe('AppsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppsService, HttpClient, HttpHandler, AppConfigService]
    });
  });

  it('should ...', inject([AppsService], (service: AppsService) => {
    expect(service).toBeTruthy();
  }));
});