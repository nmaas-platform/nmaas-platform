import { TestBed } from '@angular/core/testing';

import { NmServiceConfigService } from './nmserviceconfig.service';

describe('NmserviceconfigService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: NmServiceConfigService = TestBed.get(NmServiceConfigService);
    expect(service).toBeTruthy();
  });
});
