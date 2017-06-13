/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AppInstanceService } from './appinstance.service';

describe('AppInstanceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppInstanceService]
    });
  });

  it('should ...', inject([AppInstanceService], (service: AppInstanceService) => {
    expect(service).toBeTruthy();
  }));
});
