import { TestBed, inject } from '@angular/core/testing';

import { AppSubscriptionsService } from './appsubscriptions.service';

describe('AppSubscriptionsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppSubscriptionsService]
    });
  });

  it('should be created', inject([AppSubscriptionsService], (service: AppSubscriptionsService) => {
    expect(service).toBeTruthy();
  }));
});
