import {TranslateLoader} from "@ngx-translate/core";
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "../service/appconfig.service";
import {Observable} from "rxjs";
import {catchError} from 'rxjs/operators';


export class TranslateLoaderImpl implements TranslateLoader{

    constructor(public http: HttpClient, public appConfig:AppConfigService){};

    getTranslation(lang: string): Observable<any>{
        return this.http.get<string>(this.appConfig.getApiUrl() + '/i18n/content/' + lang).pipe(
            catchError(() => this.http.get<string>('./assets/i18n/' + lang + '.json')));
    }
}
