import { TestBed } from '@angular/core/testing';

import { ConfigTemplateService } from './configtemplate.service';

describe('ConfigtemplateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ConfigTemplateService = TestBed.get(ConfigTemplateService);
    expect(service).toBeTruthy();
  });
});
