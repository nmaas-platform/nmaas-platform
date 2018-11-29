import {Component, OnInit, ViewChild} from '@angular/core';
import {User} from "../../model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Observable} from "../../../../node_modules/rxjs/Observable";
import {Domain} from "../../model/domain";
import {ModalComponent} from "../../shared/modal";
import {ModalInfoTermsComponent} from "../../shared/modal/modal-info-terms/modal-info-terms.component";
import {ModalInfoPolicyComponent} from "../../shared/modal/modal-info-policy/modal-info-policy.component";
import {UserService} from "../../service";
import {AuthService} from "../../auth/auth.service";
import {Router} from "@angular/router";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";

@Component({
  selector: 'app-terms-acceptance',
  templateUrl: './terms-acceptance.component.html',
  styleUrls: ['./terms-acceptance.component.css'],
    providers: [ModalInfoPolicyComponent, ModalInfoTermsComponent, ModalComponent]
})
export class TermsAcceptanceComponent extends BaseComponent implements OnInit {

    public user:User;
    public registrationForm: FormGroup;
    public domains: Observable<Domain[]>;
    public errorMessage: string = '';
    public sending: boolean = false;
    public submitted: boolean = false;
    public success: boolean = false;

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @ViewChild(ModalInfoTermsComponent)
    public readonly modalInfoTerms: ModalInfoTermsComponent;

    @ViewChild(ModalInfoPolicyComponent)
    public readonly modalInfoPolicy: ModalInfoPolicyComponent;

    constructor(private fb: FormBuilder, private userService: UserService,  private auth: AuthService, private router: Router) {
        super();
        this.registrationForm = fb.group(
            {
                termsOfUseAccepted: [true],
                privacyPolicyAccepted: [false],
            });
    }


    ngOnInit() {
        this.modal.setModalType("success");
        this.modal.setStatusOfIcons(false);
    }

    public onSubmit(): void {
        if (!this.registrationForm.controls['termsOfUseAccepted'].value || !this.registrationForm.controls['privacyPolicyAccepted'].value){
            this.sending = false;
            this.submitted = true;
            this.success = false;
            this.errorMessage = "You have to accept Terms of Use and Privacy Policy!"
        }else{
          this.userService.completeAcceptance(this.auth.getUsername()).subscribe(
              (result) => {
                  console.log("Data updated successfully.");
                  this.success = true;
                  this.auth.logout();
                  this.modal.show();
              },
              (err) => {
                  console.log("Unable to change acceptance of terms.");
                  this.sending = false;
                  this.submitted = true;
                  this.success = false;
                  this.errorMessage = err.statusCode==406?'Invalid input data':'Service is unavailable. Please try again later';
              },
              () => {
                  this.sending = false;
                  this.submitted = true;
              }
          );
        }
    }

    public hide(): void{
      this.router.navigate(['/welcome/login']);
    }
}
