import {RegistrationService} from '../../auth/registration.service';
import {Domain} from '../../model/domain';
import {Registration} from '../../model/registration';
import {AppConfigService} from '../../service/appconfig.service';
import {PasswordValidator} from '../../shared/common/password/password.component';
import {AfterContentInit, AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {FormGroup, FormBuilder, Validators} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {ModalInfoTermsComponent} from "../../shared/modal/modal-info-terms/modal-info-terms.component";
import {ModalInfoPolicyComponent} from "../../shared/modal/modal-info-policy/modal-info-policy.component";
import {ModalComponent} from "../../shared/modal";

import {ReCaptchaComponent, ReCaptchaModule} from "angular5-recaptcha";
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'nmaas-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css'],
    providers: [ModalComponent, ModalInfoTermsComponent, ModalInfoPolicyComponent]
})
export class RegistrationComponent implements OnInit {


  public sending: boolean = false;
  public submitted: boolean = false;
  public success: boolean = false;
  public errorMessage: string = '';

  @ViewChild(ReCaptchaComponent)
  captcha: ReCaptchaComponent;

  @ViewChild(ModalComponent)
  public readonly  modal: ModalComponent;

  @ViewChild(ModalInfoTermsComponent)
  public readonly modalInfoTerms: ModalInfoTermsComponent;

  @ViewChild(ModalInfoPolicyComponent)
  public readonly modalInfoPolicy: ModalInfoPolicyComponent;

  public registrationForm: FormGroup;
  public domains: Observable<Domain[]>;

  private readonly language: string = '';

  constructor(private fb: FormBuilder,
              private registrationService: RegistrationService,
              private appConfig: AppConfigService,
              private translate: TranslateService) {
    this.registrationForm = fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(3)]],
        newPassword: ['', Validators.required],
        confirmPassword: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        firstname: [''],
        lastname: [''],
        domainId: [null],
          termsOfUseAccepted: [true],
          privacyPolicyAccepted: [false],
      },
      {
        validator: PasswordValidator.match
      });

  }

  ngOnInit() {
    this.domains = this.registrationService.getDomains()
      .map((domains) => domains.filter((domain) => domain.id !== this.appConfig.getNmaasGlobalDomainId()));
  }

  public onSubmit(): void {
      let token = this.captcha.getResponse();
      if(token.length < 1){
          this.sending = false;
          this.sending = false;
          this.submitted = true;
          this.success = false;
          this.errorMessage = this.translate.instant('GENERIC_MESSAGE.NOT_ROBOT_ERROR_MESSAGE');
      } else {
          if (!this.registrationForm.controls['termsOfUseAccepted'].value ||
            !this.registrationForm.controls['privacyPolicyAccepted'].value) {
              this.sending = false;
              this.submitted = true;
              this.success = false;
              this.errorMessage = this.translate.instant('GENERIC_MESSAGE.TERMS_OF_USER_MESSAGE');
          } else {
              if (this.registrationForm.valid) {
                  this.sending = true;

                  const registration: Registration = new Registration(
                      this.registrationForm.controls['username'].value,
                      this.registrationForm.controls['newPassword'].value,
                      this.registrationForm.controls['email'].value,
                      this.registrationForm.controls['firstname'].value,
                      this.registrationForm.controls['lastname'].value,
                      this.registrationForm.controls['domainId'].value,
                      true,
                      this.registrationForm.controls['privacyPolicyAccepted'].value,
                  );
                  this.registrationService.register(registration).subscribe(
                      (result) => {
                          this.registrationForm.reset();
                          this.sending = false;
                          this.submitted = true;
                          this.success = true;
                          this.modal.show();
                      },
                      (err) => {
                          this.sending = false;
                          this.submitted = true;
                          this.success = false;
                          this.errorMessage = this.translate.instant(this.getMessage(err));
                      },
                      () => {
                          this.sending = false;
                          this.submitted = true;
                      }
                  );

              }
          }
      }
  }

  public refresh(): void {
    this.sending = false;
    this.submitted = false;
    this.success = false;
    this.errorMessage = '';
  }

  private getMessage(err: string): string {
      return err.match('') || err.match(undefined) || err.match(null) ? err : 'GENERIC_MESSAGE.UNAVAILABLE_MESSAGE';
  }
}
