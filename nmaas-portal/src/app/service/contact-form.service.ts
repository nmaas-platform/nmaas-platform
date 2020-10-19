import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {AccessModifier, ContactFormType} from '../model/contact-form-type';

@Injectable({
    providedIn: 'root'
})
export class ContactFormService {

    private readonly formsPath = 'assets/contact/formio/'
    private readonly default = 'default';

    /**
     * provide default static list of possible contact forms to be selected
     * consider loading it from static json instead of hard coding into function code
     * for static serving purposes
     */
    private readonly formTypes: ContactFormType[] = [
        {key: 'CONTACT', access: AccessModifier.ALL, templateName: this.default},
        {key: 'ISSUE', access: AccessModifier.ALL, templateName: this.default},
        {key: 'FEATURE_REQUEST', access: AccessModifier.ALL, templateName: this.default},
        {key: 'ACCESS_REQUEST', access: AccessModifier.ONLY_NOT_LOGGED_IN, templateName: this.default}
    ]

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

    public getAllFormTypes(): Observable<ContactFormType[]> {
        return of(this.formTypes)
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
