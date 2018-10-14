/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AppConfigService } from './appconfig.service';
import {HttpClient, HttpHandler} from "@angular/common/http";

describe('Service: AppConfig', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppConfigService, HttpClient, HttpHandler]
    });
  });

  it('should ...', inject([AppConfigService], (service: AppConfigService) => {
    expect(service).toBeTruthy();
  }));
});
