import { TestBed } from '@angular/core/testing';

import { MailTemplateService } from './mailtemplate.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AppConfigService} from "./appconfig.service";

describe('MailtemplateService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [AppConfigService]
  }));

  it('should be created', () => {
    const service: MailTemplateService = TestBed.get(MailTemplateService);
    expect(service).toBeTruthy();
  });
});
