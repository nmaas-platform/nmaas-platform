import { TestBed, inject } from '@angular/core/testing';

import { CacheService } from './cache.service';

describe('CacheService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CacheService]
    });
  });

  it('should be created', inject([CacheService], (service: CacheService<number, string>) => {
    expect(service).toBeTruthy();
  }));
});
