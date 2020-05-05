import { TestBed } from '@angular/core/testing';

import { SSEService } from './sse.service';

describe('SSEService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SSEService = TestBed.get(SSEService);
    expect(service).toBeTruthy();
  });
});
