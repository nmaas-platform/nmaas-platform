import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class ConfigTemplateService {

  public configTemplate: any;

  public configUpdateTemplate: any;

  public defaultElement: any;

  public basicAuth: any;

  constructor(public http: HttpClient) { }

  public loadConfigTemplate() {
    this.http.get('/assets/formio/config-template.json')
       .subscribe(configTemplate => {
       this.configTemplate = configTemplate;
    });

    this.http.get('/assets/formio/config-update-template.json')
        .subscribe(configUpdateTemplate => {
          this.configUpdateTemplate = configUpdateTemplate;
    });

    this.http.get('/assets/formio/defaultElement.json')
        .subscribe(configUpdateTemplate => {
          this.defaultElement = configUpdateTemplate;
        });

    this.http.get('/assets/formio/basicAuth.json')
        .subscribe(auth => this.basicAuth = auth);
  }

  public getConfigTemplate(): any {
    return this.configTemplate;
  }

  public getConfigUpdateTemplate(): any {
    return this.configUpdateTemplate;
  }

  public getDefaultElement(): any {
    return this.defaultElement;
  }

  public getBasicAuth(appName: string): any {
    let temp = JSON.stringify(this.basicAuth);
    temp = temp.replace(/@APP_NAME/g, appName);
    return JSON.parse(temp);
  }
}
