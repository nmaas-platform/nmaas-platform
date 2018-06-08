import {Injectable} from '@angular/core';
import {GenericDataService} from "./genericdata.service";
import {AuthHttp} from "angular2-jwt";
import {AppConfigService} from "./appconfig.service";
import {JsonMapperService} from "./jsonmapper.service";
import {Observable} from "rxjs/Observable";
import {User} from "../model";

@Injectable()
export class ProfileService extends GenericDataService {

  constructor(authHttp: AuthHttp, appConfig: AppConfigService, private jsonModelMapper: JsonMapperService) {
    super(authHttp, appConfig)
  }

  public getOne():Observable<User>{
    return this.get<User>(this.getProfileUrl()+'user').map((user) => this.jsonModelMapper.deserialize(user, User))
  }

  protected getProfileUrl(): string{
      return this.appConfig.getApiUrl()+'/profile/'
  }
}
