import { TestBed, inject } from '@angular/core/testing';

import { MonitorService } from './monitor.service';

describe('MonitorService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MonitorService]
    });
  });

  it('should be created', inject([MonitorService], (service: MonitorService) => {
    expect(service).toBeTruthy();
  }));
});
