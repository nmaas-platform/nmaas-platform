import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';


@Injectable({
    providedIn: 'root',
})
export class AppConfigService {
    config: any;

    public jwtAllowedDomains: string[] = []

    constructor(private http: HttpClient) { }

    public load() {
        return new Promise((resolve) => {
            this.http.get('config.json')
                .subscribe(config => {
                    this.config = config;
                    this.jwtAllowedDomains.push(this.getApiUrl().replace('http://', '').replace('https://', '').split(/[/?#]/)[0]);
                    resolve();
                });
        });
    }

    public getApiUrl(): string {
      if (this.config == null) {
        return 'http://localhost/api';
      }
      return this.config.apiUrl;
    }

    public getNmaasGlobalDomainId(): number {
      if (this.config == null) {
         return 0;
      }
      return this.config.nmaas.globalDomainId || 0;
    }

    public getHttpTimeout(): number {
      if (this.config == null) {
         return 10000;
      }
      return this.config.http.timeout || 10000;
    }

    public getShowGitInfo(): boolean {
      if (this.config == null) {
          return false;
      }
      return this.config.showGitInfo || false;
    }

    public getShowChangelog(): boolean {
      if (this.config == null) {
          return false;
      }
      return this.config.showChangelog || false;
    }

    public getSiteKey(): string {
        if (this.config == null) {
            return '';
        }
        return this.config.captchaKey || '';
    }

    public getTestInstanceModalKey(): string {
        return 'test_instance_modal';
}
}
