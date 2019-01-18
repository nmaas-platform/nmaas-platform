import {Component, OnInit, ViewChild, Input} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs";
import {Domain} from "../../model/domain";
import {RegistrationService} from "../../auth/registration.service";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {Router} from "@angular/router";
import {AuthService} from "../../auth/auth.service";
import {ModalComponent} from "../../shared/modal";
import {ModalInfoTermsComponent} from "../../shared/modal/modal-info-terms/modal-info-terms.component";
import {ModalInfoPolicyComponent} from "../../shared/modal/modal-info-policy/modal-info-policy.component";
import {TranslateService} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {UserService} from "../../service";

@Component({
    selector: 'app-complete',
    templateUrl: './complete.component.html',
    styleUrls: ['./complete.component.css'],
    providers:[ProfileService, UserService, ModalInfoTermsComponent, ModalInfoPolicyComponent]
})

export class CompleteComponent extends BaseComponent implements OnInit {

    public user: User;
    public registrationForm: FormGroup;
    public domains: Observable<Domain[]>;
    public errorMessage: string = '';
    public sending: boolean = false;
    public submitted: boolean = false;
    public success: boolean = false;

    public languages: string[];

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @ViewChild(ModalInfoTermsComponent)
    public readonly modalInfoTerms: ModalInfoTermsComponent;

    @ViewChild(ModalInfoPolicyComponent)
    public readonly modalInfoPolicy: ModalInfoPolicyComponent;

    constructor(private fb: FormBuilder,
                protected userService: UserService,
                protected profileService: ProfileService,
                private authService: AuthService,
                private router: Router,
                private translate: TranslateService,
                private contentService: ContentDisplayService) {
        super();
        this.registrationForm = fb.group(
            {
                username: ['', [Validators.required, Validators.minLength(3)]],
                email: ['', [Validators.required, Validators.email]],
                firstname: [''],
                lastname: [''],
                termsOfUseAccepted: [true],
                privacyPolicyAccepted: [false]
            });
    }

    ngOnInit() {
        this.contentService.getLanguages().subscribe(langs => this.languages = langs);
        this.profileService.getOne().subscribe((user) => this.user = user);
        this.modal.setStatusOfIcons(false);
        this.modal.setModalType("success");
    }

    public onSubmit(): void {
        if (!this.registrationForm.controls['termsOfUseAccepted'].value || !this.registrationForm.controls['privacyPolicyAccepted'].value) {
            this.sending = false;
            this.submitted = true;
            this.success = false;
            this.errorMessage = this.translate.instant('GENERIC_MESSAGE.TERMS_OF_USER_MESSAGE');
        } else {
            if (this.registrationForm.valid) {
                this.user.enabled = false;
                this.user.username = this.registrationForm.controls['username'].value;
                this.user.email = this.registrationForm.controls['email'].value;
                this.user.firstname = this.registrationForm.controls['firstname'].value;
                this.user.lastname = this.registrationForm.controls['lastname'].value;
                this.user.termsOfUseAccepted = true;
                this.user.privacyPolicyAccepted = this.registrationForm.controls['privacyPolicyAccepted'].value;

                this.userService.completeRegistration(this.user).subscribe(
                    (result) => {
                        this.success = true;
                        this.authService.logout();
                        this.modal.show();
                    },
                    (err) => {
                        this.sending = false;
                        this.submitted = true;
                        this.success = false;
                        this.errorMessage = err.statusCode === 406 ?
                            this.translate.instant('GENERIC_MESSAGE.INVALID_INPUT_MESSAGE') :
                            this.translate.instant('GENERIC_MESSAGE.UNAVAILABLE_MESSAGE');
                    },
                    () => {
                        this.sending = false;
                        this.submitted = true;
                    }
                );


            }
        }
    }

    public useLanguage(language: string) {
        this.translate.use(language);
    }

    public hide(): void{
        this.router.navigate(['/']);
    }

}
