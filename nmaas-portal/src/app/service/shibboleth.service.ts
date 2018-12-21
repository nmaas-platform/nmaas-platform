import { Injectable } from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "rxjs";
import {ShibbolethConfig} from "../model/shibboleth";

@Injectable()
export class ShibbolethService extends GenericDataService{

  protected url:string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.url = this.appConfig.getApiUrl() + '/management/shibboleth/';
  }

  public getOne():Observable<ShibbolethConfig>{
    return this.get<ShibbolethConfig>(this.url);
  }

}
