
import {throwError as observableThrowError,  Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';

import { AppConfigService } from '../service/appconfig.service';
import {catchError} from 'rxjs/operators';





@Injectable()
export class TagService {
    
  constructor(private http : HttpClient, private appConfig: AppConfigService) { }

  public getTags() : Observable<string[]> {
      return this.http.get<string[]>(this.appConfig.getApiUrl()+'/tags').pipe(
          catchError((error:any) => observableThrowError(error.json().message || 'Server error')));
  }
}
