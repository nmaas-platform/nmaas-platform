import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class ConfigTemplateService {

  public configTemplate: any;

  public configUpdateTemplate: any;

  constructor(public http:HttpClient) { }

  public loadConfigTemplate() {
    this.http.get('/assets/formio/config-template.json')
       .subscribe(configTemplate => {
       this.configTemplate = configTemplate;
    });

    this.http.get('/assets/formio/config-update-template.json')
        .subscribe(configUpdateTemplate => {
          this.configUpdateTemplate = configUpdateTemplate;
        });
  }

  public getConfigTemplate(): any {
    return this.configTemplate;
  }

  public getConfigUpdateTemplate(): any {
    return this.configUpdateTemplate;
  }
}
