import { TestBed } from '@angular/core/testing';

import { ShellClientService } from './shell-client.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('ShellClientService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
        HttpClientTestingModule,
    ]
  }));

  it('should be created', () => {
    const service: ShellClientService = TestBed.get(ShellClientService);
    expect(service).toBeTruthy();
  });
});
