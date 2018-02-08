import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AppConfigService {
    config: any;

    constructor(private http: Http) { }

    public load() {
        return new Promise((resolve) => {
            this.http.get('config.json').map(res => res.json())
                .subscribe(config => {
                    this.config = config;
                    resolve();
                });
        });
    }

    public getApiUrl(): string {
        return this.config.apiUrl || 'http://localhost/portal/api';
    }

    public getNmaasGlobalDomainId(): number {
      return this.config.nmaas.globalDomainId || 0;
    }

    public getHttpTimeout(): number {
      return this.config.http.timeout || 10000;
    }
}
