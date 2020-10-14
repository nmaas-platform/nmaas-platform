import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ContactFormService {

    private readonly formsPath = 'assets/contact/formio/'
    private readonly default = 'default';

    constructor(private http: HttpClient) {
    }

    public getForm(name: string): Observable<any> {
        return this.http.get(this.formsPath + name + '.json').pipe(
            catchError(
                err => {
                    console.error('ERROR getting contact form template', err)
                    return this.http.get(this.formsPath + this.default + '.json')
                }
            )
        )
    }
}
