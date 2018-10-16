/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AppInstanceService } from './appinstance.service';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";

describe('AppInstanceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppInstanceService, HttpHandler, HttpClient, AppConfigService]
    });
  });

  it('should ...', inject([AppInstanceService], (service: AppInstanceService) => {
    expect(service).toBeTruthy();
  }));
});
