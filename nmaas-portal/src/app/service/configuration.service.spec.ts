import {inject, TestBed} from '@angular/core/testing';

import {ConfigurationService} from './configuration.service';
import {AppConfigService} from "./appconfig.service";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {Observable} from "rxjs";
import {Configuration} from "../model/configuration";

class MockConfigurationService{
    protected uri:string;

    constructor() {
        this.uri = 'http://localhost/api';
    }

    public getApiUrl(): string {
        return 'http://localhost/api';
    }

    public getConfiguration():Observable<Configuration>{
        return Observable.of<Configuration>();
    }

    public updateConfiguration(configuration:Configuration):Observable<any>{
        return Observable.of<Configuration>();
    }
}

describe('ConfigurationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConfigurationService, HttpHandler, HttpClient, {provide: AppConfigService, useClass: MockConfigurationService}]
    });
  });

  it('should be created', inject([ConfigurationService], (service: ConfigurationService) => {
    expect(service).toBeTruthy();
  }));
});
