import { TestBed, inject } from '@angular/core/testing';

import { SSOService } from './sso.service';
import {Observable, of} from 'rxjs';
import {Configuration} from '../model/configuration';
import {HttpClient, HttpHandler} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';

class MockConfigurationService {
    protected uri: string;

    constructor() {
        this.uri = 'http://localhost/api';
    }

    public getApiUrl(): string {
        return 'http://localhost/api';
    }

    public getConfiguration(): Observable<Configuration> {
        return of<Configuration>();
    }

    public updateConfiguration(configuration: Configuration): Observable<any> {
        return of<Configuration>();
    }
}

describe('SSOService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SSOService, HttpHandler, HttpClient, {provide: AppConfigService, useClass: MockConfigurationService}]
    });
  });

  it('should be created', inject([SSOService], (service: SSOService) => {
    expect(service).toBeTruthy();
  }));
});
