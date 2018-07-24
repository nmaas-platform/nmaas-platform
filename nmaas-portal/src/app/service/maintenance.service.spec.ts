import {inject, TestBed} from '@angular/core/testing';

import {MaintenanceService} from './maintenance.service';

describe('MaintenanceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaintenanceService]
    });
  });

  it('should be created', inject([MaintenanceService], (service: MaintenanceService) => {
    expect(service).toBeTruthy();
  }));
});
