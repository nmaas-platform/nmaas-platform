import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {AccessModifier, ContactFormType} from '../model/contact-form-type';

@Injectable({
    providedIn: 'root'
})
export class ContactFormService {

    private readonly formsPath = 'assets/contact/'
    private readonly default = 'default';

    constructor(private http: HttpClient) {
    }

    public getForm(name: string): Observable<any> {
        return this.http.get(this.formsPath + 'formio/' + name + '.json').pipe(
            catchError(
                err => {
                    console.error('ERROR getting contact form template', err)
                    return this.http.get(this.formsPath + 'formio/' + this.default + '.json')
                }
            )
        )
    }

    public getAllFormTypes(): Observable<ContactFormType[]> {
        return this.http.get<ContactFormType[]>(this.formsPath + 'types.json').pipe(
            catchError(
                err => {
                    console.error('ERROR getting contact types list', err);
                    // return default form type
                    return of([
                        {key: 'CONTACT', access: AccessModifier.ALL, templateName: this.default}
                    ])
                }
            )
        )
    }

    public getAllFormTypesAsMap(): Observable<Map<string, ContactFormType>> {
        return this.getAllFormTypes().pipe(
            map(types => {
                const result = new Map<string, ContactFormType>();
                types.forEach(t => result.set(t.key, t))
                return result;
            })
        );
    }
}
