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

  public getAll():Observable<ShibbolethConfig[]>{
    return this.get<ShibbolethConfig[]>(this.url+'list');
  }

  public getOne():Observable<ShibbolethConfig>{
    return this.get<ShibbolethConfig>(this.url);
  }

  public getOneById(config_id:number):Observable<ShibbolethConfig>{
    return this.get<ShibbolethConfig>(this.url + config_id);
  }

  public add(shibbolethConfig:ShibbolethConfig):Observable<any>{
    return this.post(this.url, shibbolethConfig);
  }

  public update(shibbolethConfig:ShibbolethConfig):Observable<any>{
    return this.put(this.url + shibbolethConfig.id, shibbolethConfig);
  }

  public remove(config_id:number):Observable<any>{
    return this.delete(this.url + config_id);
  }

}
