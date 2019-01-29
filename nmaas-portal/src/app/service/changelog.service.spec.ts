import { TestBed, inject } from '@angular/core/testing';

import { ChangelogService } from './changelog.service';
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

describe('ChangelogService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ChangelogService, HttpClient, HttpHandler, {provide: AppConfigService, useClass: MockConfigurationService}]
    });
  });

  it('should be created', inject([ChangelogService], (service: ChangelogService) => {
    expect(service).toBeTruthy();
  }));
});
