import { TestBed } from '@angular/core/testing';

import { ShellClientService } from './shell-client.service';

describe('ShellClientService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ShellClientService = TestBed.get(ShellClientService);
    expect(service).toBeTruthy();
  });
});
