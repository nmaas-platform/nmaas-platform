import {Component, OnInit, ViewChild} from '@angular/core';
import {User} from '../../model';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Domain} from '../../model/domain';
import {ModalComponent} from '../../shared/modal';
import {ModalInfoTermsComponent} from '../../shared/modal/modal-info-terms/modal-info-terms.component';
import {ModalInfoPolicyComponent} from '../../shared/modal/modal-info-policy/modal-info-policy.component';
import {UserService} from '../../service';
import {AuthService} from '../../auth/auth.service';
import {Router} from '@angular/router';
import {BaseComponent} from '../../shared/common/basecomponent/base.component';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs/internal/Observable';

@Component({
  selector: 'app-terms-acceptance',
  templateUrl: './terms-acceptance.component.html',
  styleUrls: ['./terms-acceptance.component.css'],
    providers: [ModalInfoPolicyComponent, ModalInfoTermsComponent, ModalComponent]
})
export class TermsAcceptanceComponent extends BaseComponent implements OnInit {

    public user: User;
    public registrationForm: FormGroup;
    public domains: Observable<Domain[]>;
    public errorMessage = '';
    public sending = false;
    public submitted = false;
    public success = false;

    @ViewChild(ModalComponent, { static: true })
    public readonly modal: ModalComponent;

    @ViewChild(ModalInfoTermsComponent, { static: true })
    public readonly modalInfoTerms: ModalInfoTermsComponent;

    @ViewChild(ModalInfoPolicyComponent, { static: true })
    public readonly modalInfoPolicy: ModalInfoPolicyComponent;

    constructor(private fb: FormBuilder,
                private userService: UserService,
                private auth: AuthService,
                private router: Router,
                private translate: TranslateService) {
        super();
        this.registrationForm = fb.group(
            {
                termsOfUseAccepted: [true],
                privacyPolicyAccepted: [false],
            });
    }


    ngOnInit() {
        this.modal.setModalType('success');
        this.modal.setStatusOfIcons(false);
    }

    public onSubmit(): void {
        if (!this.registrationForm.controls['privacyPolicyAccepted'].value) {
            this.sending = false;
            this.submitted = true;
            this.success = false;
            this.errorMessage = this.translate.instant('TERMS_OF_USER_MESSAGE.TERMS_OF_USER_MESSAGE')
        } else {
          this.userService.completeAcceptance(this.auth.getUsername()).subscribe(
              (result) => {
                  this.success = true;
                  this.auth.logout();
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

    public hide(): void {
      this.router.navigate(['/welcome/login']);
    }
}
