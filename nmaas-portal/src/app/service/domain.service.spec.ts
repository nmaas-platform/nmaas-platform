import { TestBed, inject } from '@angular/core/testing';

import { DomainService } from './domain.service';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
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

describe('DomainService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DomainService, HttpHandler, HttpClient, {provide: AppConfigService, useClass: MockConfigurationService}]
    });
  });

  it('should be created', inject([DomainService], (service: DomainService) => {
    expect(service).toBeTruthy();
  }));
});
