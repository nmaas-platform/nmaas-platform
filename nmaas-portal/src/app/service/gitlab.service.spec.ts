import { TestBed, inject } from '@angular/core/testing';

import { GitlabService } from './gitlab.service';

describe('GitlabService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GitlabService]
    });
  });

  it('should be created', inject([GitlabService], (service: GitlabService) => {
    expect(service).toBeTruthy();
  }));
});
