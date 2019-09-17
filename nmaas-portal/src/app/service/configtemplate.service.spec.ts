import { TestBed } from '@angular/core/testing';

import { ConfigTemplateService } from './configtemplate.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ConfigtemplateService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers:[ConfigTemplateService],
    imports:[HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: ConfigTemplateService = TestBed.get(ConfigTemplateService);
    expect(service).toBeTruthy();
  });
});
