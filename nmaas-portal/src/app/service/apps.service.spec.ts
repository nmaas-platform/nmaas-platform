/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AppsService } from './apps.service';

describe('AppsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppsService]
    });
  });

  it('should ...', inject([AppsService], (service: AppsService) => {
    expect(service).toBeTruthy();
  }));
});