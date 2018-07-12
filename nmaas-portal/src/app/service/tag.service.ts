import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';

import { AppConfigService } from '../service/appconfig.service';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class TagService {
    
  constructor(private http : HttpClient, private appConfig: AppConfigService) { }

    
    public getTags() : Observable<string[]> {
        return this.http.get(this.appConfig.getApiUrl()+'/tags')
                            .catch((error:any) => Observable.throw(error.json().message || 'Server error'));
        
    }
}
