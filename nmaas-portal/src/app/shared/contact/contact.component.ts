import {Component, EventEmitter, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../modal';
import {Mail} from '../../model/mail';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';
import {ContactFormService} from '../../service/contact-form.service';
import {ContactFormType} from '../../model/contact-form-type';
import {map, switchMap, tap} from 'rxjs/operators';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Observable} from 'rxjs';
import {AuthService} from '../../auth/auth.service';

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

    public selectForm: FormGroup;

    private readonly DEFAULT_FORM_KEY = 'CONTACT';
    private formTypesMap: Map<string, ContactFormType> = new Map();
    public formType: ContactFormType;

    constructor(private recaptchaV3Service: ReCaptchaV3Service,
                private notificationService: NotificationService,
                private contactFormProvider: ContactFormService,
                private fb: FormBuilder,
                private authService: AuthService) {
        this.selectForm = this.fb.group({
            formKey: [this.DEFAULT_FORM_KEY]
        })
    }

    ngOnInit(): void {
        this.modal.setModalType('info');
        this.contactFormProvider.getAllFormTypesAsMap().pipe(
            tap(typesMap => {this.formTypesMap = typesMap; this.formType = this.formTypesMap.get(this.DEFAULT_FORM_KEY)}),
            switchMap(typesMap => this.contactFormProvider.getForm(typesMap.get(this.DEFAULT_FORM_KEY).templateName))
        ).subscribe(
            form => this.currentFormioTemplate = form
        );

        this.selectForm.valueChanges.pipe(
            tap(result => console.log(result)),
            map(result => result.formKey),
            map(formTypeKey => this.formTypesMap.get(formTypeKey)),
            tap( formType => this.formType = formType),
            map(formType => formType.templateName),
            switchMap(formTemplateName => this.contactFormProvider.getForm(formTemplateName))
        ).subscribe(
            form => this.currentFormioTemplate = form
        )
    }

    public ready(event): void {
        console.log('Form is ready');
        this._formio = event.formio;
    }

    private sendMail(data: any): Observable<void> {
        return this.recaptchaV3Service.execute('contactForm').pipe(
            map((token) => {
                const result = {token, mail: new Mail()}
                result.mail.otherAttributes = data;
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
        return Array.from(this.formTypesMap.keys());
    }

}
