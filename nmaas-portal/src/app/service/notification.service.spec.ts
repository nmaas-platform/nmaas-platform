import { TestBed, inject } from '@angular/core/testing';

import { NotificationService } from './notification.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AppConfigService} from "./appconfig.service";

describe('NotificationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NotificationService, AppConfigService],
      imports: [HttpClientTestingModule]
    });
  });

  it('should be created', inject([NotificationService], (service: NotificationService) => {
    expect(service).toBeTruthy();
  }));
});
