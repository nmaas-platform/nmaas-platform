import {Injectable} from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "./appconfig.service";
import {Observable} from "rxjs/Observable";
import {Maintenance} from "../model/maintenance";

@Injectable()
export class MaintenanceService extends GenericDataService{

  protected uri:string;

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
    this.uri = this.appConfig.getApiUrl()+'/maintenance'
  }

  public getMaintenance():Observable<Maintenance>{
    return this.get<Maintenance>(this.uri);
  }

  public setMaintenance(flag: boolean):Observable<any>{
    return this.post(this.uri, new Maintenance(flag));
  }

}
