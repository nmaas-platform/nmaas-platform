import { TestBed, inject } from '@angular/core/testing';

import { GenericDataService } from './genericdata.service';

describe('GenericDataService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GenericDataService]
    });
  });

  it('should be created', inject([GenericDataService], (service: GenericDataService) => {
    expect(service).toBeTruthy();
  }));
});
