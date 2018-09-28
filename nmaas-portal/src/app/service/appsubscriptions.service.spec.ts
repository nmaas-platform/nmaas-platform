import { TestBed, inject } from '@angular/core/testing';

import { AppSubscriptionsService } from './appsubscriptions.service';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";

describe('AppSubscriptionsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppSubscriptionsService, HttpClient, HttpHandler, AppConfigService]
    });
  });

  it('should be created', inject([AppSubscriptionsService], (service: AppSubscriptionsService) => {
    expect(service).toBeTruthy();
  }));
});
