import { TestBed, inject } from '@angular/core/testing';

import { ShibbolethService } from './shibboleth.service';

describe('ShibbolethService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ShibbolethService]
    });
  });

  it('should be created', inject([ShibbolethService], (service: ShibbolethService) => {
    expect(service).toBeTruthy();
  }));
});
