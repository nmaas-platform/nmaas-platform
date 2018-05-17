import { TestBed, inject } from '@angular/core/testing';

import { ChangelogService } from './changelog.service';

describe('ChangelogService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ChangelogService]
    });
  });

  it('should be created', inject([ChangelogService], (service: ChangelogService) => {
    expect(service).toBeTruthy();
  }));
});
