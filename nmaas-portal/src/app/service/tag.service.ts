import { Injectable } from '@angular/core';
import { AuthHttp } from 'angular2-jwt';
import { Http, Headers, Request, Response, RequestOptions, RequestOptionsArgs} from '@angular/http';

import { AppConfigService } from '../service/appconfig.service';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class TagService {
    
  constructor(private authHttp : AuthHttp, private appConfig: AppConfigService) { }

    
    public getTags() : Observable<string[]> {
        return this.authHttp.get(this.appConfig.getApiUrl()+'/tags')
                            .map((res:Response) => res.json())
                            .catch((error:any) => Observable.throw(error.json().message || 'Server error'));
        
    }
}
