import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../modal';
import {Mail} from '../../model/mail';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {NotificationService} from '../../service/notification.service';

@Component({
    selector: 'app-contact',
    templateUrl: './contact.component.html',
    styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {

    public mail: Mail;

    public mailForm: FormGroup;

    public errorMessage: string;

    @ViewChild(ModalComponent, {static: true})
    public readonly modal: ModalComponent;

    constructor(private recaptchaV3Service: ReCaptchaV3Service,
                private notificationService: NotificationService,
                private fb: FormBuilder) {
        this.mail = new Mail();
        this.mailForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            name: ['', [Validators.required]],
            message: ['', [Validators.required, Validators.maxLength(600)]]
        });
    }

    ngOnInit(): void {
        this.modal.setModalType('info');
    }

    public sendMail() {
        if (this.mailForm.valid) {
            this.recaptchaV3Service.execute('contactForm').subscribe(
                (token) => {
                    this.mail.otherAttributes = this.mailForm.getRawValue();
                    this.mail.mailType = 'CONTACT_FORM';
                    this.notificationService.sendMail(this.mail, token).subscribe(() => {
                            this.errorMessage = undefined;
                            this.mailForm.reset();
                            this.modal.show();
                        },
                        error => this.errorMessage = error.message
                    );
                });
        }
    }

}
