import { TestBed } from '@angular/core/testing';

import { NmServiceConfigService } from './nmserviceconfig.service';
import {AppConfigService} from "./appconfig.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('NmserviceconfigService', () => {
  beforeEach(() =>  TestBed.configureTestingModule({
    providers: [NmServiceConfigService, AppConfigService],
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: NmServiceConfigService = TestBed.get(NmServiceConfigService);
    expect(service).toBeTruthy();
  });
});
