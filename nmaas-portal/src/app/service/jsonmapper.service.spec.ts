import { TestBed, inject } from '@angular/core/testing';

import { JsonMapperService } from './jsonmapper.service';

describe('JsonmapperserviceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JsonMapperService]
    });
  });

  it('should be created', inject([JsonMapperService], (service: JsonMapperService) => {
    expect(service).toBeTruthy();
  }));
});
