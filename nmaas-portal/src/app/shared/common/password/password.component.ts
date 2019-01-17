import {Component, OnInit, Input, Output, EventEmitter, ViewChild} from '@angular/core';
import {AbstractControl, Validator, Validators, FormBuilder, FormGroup} from '@angular/forms';
import {UserService} from "../../../service";
import {Password} from "../../../model";
import {ModalComponent} from "../../modal";
import {PasswordStrengthMeterComponent} from "angular-password-strength-meter";

export class PasswordValidator implements Validator {

  validate(ac: AbstractControl): {[key: string]: any} {
    const newPassword: string = ac.get('newPassword').value; // to get value in input tag
    const confirmPassword = ac.get('confirmPassword').value; // to get value in input tag
    if (newPassword !== confirmPassword) {
      ac.get('confirmPassword').setErrors({validateEqual: true})
    } else {
      return null
    }
    return null
  }
  
  static match(ac: AbstractControl) {
    return new PasswordValidator().validate(ac);
  }
}

@Component({
  selector: 'nmaas-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css'],
    providers: [ModalComponent]
})
export class PasswordComponent implements OnInit {

  @ViewChild(ModalComponent)
  public readonly modal:ModalComponent;

  @ViewChild(PasswordStrengthMeterComponent)
  passwordMeter: PasswordStrengthMeterComponent;

  public passwordForm: FormGroup;

  public errormsg:string;

  constructor(private fb: FormBuilder, private userService:UserService) {
    this.passwordForm = fb.group(
      {
        password: ['', Validators.required],
        newPassword:['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required]
      },
      {
        validator: PasswordValidator.match
      })
  }

  ngOnInit() {

  }

  public submit(): void {
    if(this.passwordForm.valid){
        this.userService.changePassword(new Password(this.passwordForm.controls['password'].value, this.passwordForm.controls['newPassword'].value)).subscribe((value)=>{
          this.hide();
        },(err)=>{
          this.errormsg = err.message?err.message:err.statusMessage;
        })
    }
  }

  public show(): void{
    this.modal.show();
  }

  public hide():void{
    this.modal.hide();
    this.errormsg = undefined;
    this.passwordForm.reset();
  }
}
