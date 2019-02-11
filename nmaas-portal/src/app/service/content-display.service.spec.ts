import { TestBed, inject } from '@angular/core/testing';

import { ContentDisplayService } from './content-display.service';
import {ConfigurationService} from "./configuration.service";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable, of} from "rxjs";
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
        return of<Configuration>();
    }

    public updateConfiguration(configuration:Configuration):Observable<any>{
        return of<Configuration>();
    }
}

describe('ContentDisplayService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ContentDisplayService, HttpHandler, HttpClient, {provide: AppConfigService, useClass: MockConfigurationService}]
    });
  });

  it('should be created', inject([ContentDisplayService], (service: ContentDisplayService) => {
    expect(service).toBeTruthy();
  }));
});
