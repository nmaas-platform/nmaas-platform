import {TranslateLoader} from "@ngx-translate/core";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "../service/appconfig.service";
import {Observable} from "rxjs";
import {catchError} from 'rxjs/operators';
import {ServiceUnavailableService} from "../service-unavailable/service-unavailable.service";

export class TranslateLoaderImpl implements TranslateLoader{

    constructor(public http: HttpClient, public appConfig:AppConfigService, public serviceAvailability: ServiceUnavailableService){};

    getTranslation(lang: string): Observable<any>{
        //console.debug(this.serviceAvailability.isServiceAvailable);
        if(!this.serviceAvailability.isServiceAvailable){
            return this.http.get<string>('./assets/i18n/' + lang + '.json').pipe();
        }else {
            return this.http.get<string>(this.appConfig.getApiUrl() + '/i18n/content/' + lang).pipe(
              catchError(() => this.http.get<string>('./assets/i18n/' + lang + '.json')));
        }
    }
}
