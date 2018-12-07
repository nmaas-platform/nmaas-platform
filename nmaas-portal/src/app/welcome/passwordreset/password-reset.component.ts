import {Component, OnInit, ViewChild} from '@angular/core';
import {UserService} from "../../service";
import {ActivatedRoute, Router} from "@angular/router";
import {User} from "../../model";
import {PasswordReset} from "../../model/passwordreset";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {PasswordValidator} from "../../shared";
import {ReCaptchaComponent} from "angular5-recaptcha";
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-passwordreset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.css'],
  providers: [UserService]
})
export class PasswordResetComponent implements OnInit {

  public token:string;

  public user:User;

  public passwordReset:PasswordReset = new PasswordReset();

  @ViewChild(ReCaptchaComponent)
  public captcha: ReCaptchaComponent;

  public form: FormGroup;

  public errorMessage: string;

  constructor(private fb: FormBuilder,
              private userService: UserService,
              private router: Router,
              private route: ActivatedRoute,
              private translate: TranslateService) {
      this.form = fb.group(
          {
              newPassword: ['', Validators.required],
              confirmPassword: ['', Validators.required]
          },
          {
              validator: PasswordValidator.match
          });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.token = params['token'];
      this.userService.validateResetRequest(params['token'])
          .subscribe(result => this.user = result, error1 => this.errorMessage = error1.message);
    });
  }

  public resetPassword(){
      let token = this.captcha.getResponse();
      if(token.length < 1){
          this.errorMessage = this.translate.instant('GENERIC_MESSAGE.NOT_ROBOT_ERROR_MESSAGE');
      } else {
          if(this.form.valid){
              this.passwordReset.password = this.form.controls['newPassword'].value;
              this.passwordReset.token = this.token;
              this.userService.resetPassword(this.passwordReset).subscribe(()=>this.router.navigate(['/']),err=>this.errorMessage = err.message);
          }
      }
  }

  public goBack(){
      this.router.navigate(['/']);
  }

}
