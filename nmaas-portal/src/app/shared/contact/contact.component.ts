import {Component, EventEmitter, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../modal';
import {Mail} from '../../model/mail';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';
import {ContactFormService} from '../../service/contact-form.service';
import {AccessModifier, ContactFormType, parseAccessModifier} from '../../model/contact-form-type';
import {map, switchMap, tap} from 'rxjs/operators';
import {FormBuilder, FormGroup} from '@angular/forms';
import {forkJoin, Observable} from 'rxjs';
import {AuthService} from '../../auth/auth.service';
import {InternationalizationService} from '../../service/internationalization.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-contact',
    templateUrl: './contact.component.html',
    styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {

    @ViewChild(ModalComponent, {static: true})
    public readonly modal: ModalComponent;

    private _formio: any;
    public currentFormioTemplate: any;
    public successEmitter: EventEmitter<any> = new EventEmitter<any>();
    public refreshForm: EventEmitter<any> = new EventEmitter<any>();
    public langChangeEventEmitter: EventEmitter<string> = new EventEmitter<string>()

    public selectForm: FormGroup;

    private readonly DEFAULT_FORM_KEY = 'CONTACT';
    private formTypesMap: Map<string, ContactFormType> = new Map();
    public formType: ContactFormType;

    // dummy translation file
    public i18n: any = undefined;

    constructor(private recaptchaV3Service: ReCaptchaV3Service,
                private notificationService: NotificationService,
                private contactFormProvider: ContactFormService,
                private fb: FormBuilder,
                private authService: AuthService,
                private langService: InternationalizationService,
                private translateService: TranslateService) {
        this.selectForm = this.fb.group({
            formKey: [this.DEFAULT_FORM_KEY]
        })
    }

    ngOnInit(): void {
        this.modal.setModalType('info');
        // download types map, set default form values
        this.contactFormProvider.getAllFormTypesAsMap().pipe(
            tap(typesMap => {
                this.formTypesMap = typesMap;
                this.formType = this.formTypesMap.get(this.DEFAULT_FORM_KEY)
            }),
            switchMap(typesMap => this.contactFormProvider.getForm(typesMap.get(this.DEFAULT_FORM_KEY).templateName))
        ).subscribe(
            form => this.currentFormioTemplate = form
        );

        // subscribe to form changes
        this.selectForm.valueChanges.pipe(
            tap(result => console.log(result)),
            map(result => result.formKey), // retrieve form key
            map(formTypeKey => this.formTypesMap.get(formTypeKey)), // retrieve related formType object
            tap(formType => this.formType = formType), // set most recent formType
            map(formType => formType.templateName), // retrieve template name
            switchMap(formTemplateName => this.contactFormProvider.getForm(formTemplateName)) // retrieve form value
        ).subscribe(
            form => this.currentFormioTemplate = form
        );

        this.translateService.onLangChange.pipe(
            map(event => event.lang)
        ).subscribe(this.langChangeEventEmitter);

        this.langService.getEnabledLanguages().pipe(
            map((langs: string[]) => langs.map(lang => {
                const t = {};
                t[lang] = this.translateService.getTranslation(lang)
                return t;
            })),
            map((langs: string[]) => langs.reduce((r, c) => Object.assign(r, c), {})),
            switchMap((langs: any) => forkJoin(langs)),
            map(value => {
                Object.keys(value).forEach(k => {
                    value[k] = value[k]['CONTACT_FORM']['FORMIO']
                })
                return value;
            }),
            tap(value => console.log(value))
        ).subscribe(
            value => this.i18n = value
        )
    }

    public ready(event): void {
        console.log('Form is ready');
        this._formio = event.formio;
    }

    private sendMail(data: any): Observable<void> {
        // submit captcha request
        return this.recaptchaV3Service.execute('contactForm').pipe(
            map((token) => {
                const result = {token, mail: new Mail()} // create mail object
                result.mail.otherAttributes = data; // set properties and mail attributes
                result.mail.otherAttributes.subject = this.formType.emailSubject;
                result.mail.otherAttributes.subType = this.formType.key;
                result.mail.mailType = 'CONTACT_FORM';
                return result;
            }),
            switchMap(arg => this.notificationService.sendMail(arg.mail, arg.token))
        );
    }

    public onSubmit(data: any): void {
        console.log('On submit', data);
        this.sendMail(data).subscribe(
            () => {
                this.modal.show();
                this.successEmitter.emit('Success!');
                this._formio.emit('submitDone');
            },
            error => console.error(error)
        );

        // TODO find proper way to reset form
        setTimeout(() => this.refreshForm.emit({
            form: this.currentFormioTemplate
        }), 5000)
    }

    public getFormOptions(): string[] {
        return Array.from(this.formTypesMap.keys()).filter(key => this.shouldDisplayFormType(key));
    }

    public shouldDisplayFormType(value: string): boolean {
        const access: AccessModifier = parseAccessModifier(this.formTypesMap.get(value).access);
        switch (access) {
            case AccessModifier.ALL:
                return true;
            case AccessModifier.ONLY_LOGGED_IN:
                return this.authService.isLogged();
            case AccessModifier.ONLY_NOT_LOGGED_IN:
                return !this.authService.isLogged();
            default:
                console.log('Something went wrong')
                return false;
        }
    }

    public getCurrentLang(): string {
        return this.translateService.currentLang;
    }

}
