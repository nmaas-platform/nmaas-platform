import { TestBed, inject } from '@angular/core/testing';

import { ClusterService } from './cluster.service';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {Observable} from "rxjs";
import {Configuration} from "../model/configuration";
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

describe('ClusterService', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [ClusterService, HttpClient, HttpHandler, {provide: AppConfigService, useClass: MockConfigurationService}]
        });
    });

    it('should be created', inject([ClusterService], (service: ClusterService) => {
        expect(service).toBeTruthy();
    }));
});