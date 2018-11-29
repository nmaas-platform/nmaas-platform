import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {isNullOrUndefined} from "util";

@Injectable()
export class AppConfigService {
    config: any;

    constructor(private http: HttpClient) { }

    public load() {
        return new Promise((resolve) => {
            this.http.get('config.json')
                .subscribe(config => {
                    this.config = config;
                    resolve();
                });
        });
    }

    public getApiUrl(): string {
      if(isNullOrUndefined(this.config)){
          return 'http://localhost/api';
      }
      return this.config.apiUrl || 'http://localhost/api';
    }

    public getNmaasGlobalDomainId(): number {
      if(isNullOrUndefined(this.config)){
         return 0;
      }
      return this.config.nmaas.globalDomainId || 0;
    }

    public getHttpTimeout(): number {
      if(isNullOrUndefined(this.config)){
         return 10000;
      }
      return this.config.http.timeout || 10000;
    }
}
