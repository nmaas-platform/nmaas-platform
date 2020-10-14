import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../modal';
import {Mail} from '../../model/mail';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';
import {ContactFormService} from '../../service/contact-form.service';

@Component({
    selector: 'app-contact',
    templateUrl: './contact.component.html',
    styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {

    public mail: Mail;

    @ViewChild(ModalComponent, {static: true})
    public readonly modal: ModalComponent;

    public currentForm: any;
    public currentFormTemplate: any;

    constructor(private recaptchaV3Service: ReCaptchaV3Service,
                private notificationService: NotificationService,
                private contactFormProvider: ContactFormService) {
        this.mail = new Mail();
    }

    ngOnInit(): void {
        this.modal.setModalType('info');
        this.contactFormProvider.getForm('default').subscribe(
            form => this.currentFormTemplate = form
        );
    }

    public ready(event): void {
        console.log('Form is ready');
        this.currentForm = event.formio;
    }

    private sendMail(data: any) {
        this.recaptchaV3Service.execute('contactForm').subscribe(
            (token) => {
                this.mail.otherAttributes = data;
                this.mail.mailType = 'CONTACT_FORM';
                this.notificationService.sendMail(this.mail, token).subscribe(
                    () => this.modal.show(),
                    error => console.error(error)
                );
            });
    }

    public onSubmit(data: any): void {
        console.log('On submit', data);
        this.sendMail(data);
        this.currentForm.emit('submitDone');
        // TODO find proper way to reset form
        setTimeout(this.currentForm.emit('reset'), 5000)
    }

}
