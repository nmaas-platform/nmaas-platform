import { TestBed, inject } from '@angular/core/testing';

import { ContentDisplayService } from './content-display.service';

describe('ContentDisplayService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ContentDisplayService]
    });
  });

  it('should be created', inject([ContentDisplayService], (service: ContentDisplayService) => {
    expect(service).toBeTruthy();
  }));
});
