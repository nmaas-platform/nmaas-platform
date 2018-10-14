import { TestBed, inject } from '@angular/core/testing';

import { GenericDataService } from './genericdata.service';
import {Observable} from "rxjs";
import {Configuration} from "../model/configuration";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";

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

describe('GenericDataService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GenericDataService, HttpHandler, HttpClient, {provide: AppConfigService, useClass: MockConfigurationService}]
    });
  });

  it('should be created', inject([GenericDataService], (service: GenericDataService) => {
    expect(service).toBeTruthy();
  }));
});
