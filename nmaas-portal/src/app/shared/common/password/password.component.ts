import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {AbstractControl, Validator, Validators, FormBuilder, FormGroup} from '@angular/forms';

export class PasswordValidator implements Validator {

  validate(ac: AbstractControl): {[key: string]: any} {
    const password: string = ac.get('password').value; // to get value in input tag
    const confirmPassword = ac.get('confirmPassword').value; // to get value in input tag
    if (password !== confirmPassword) {
      ac.get('confirmPassword').setErrors({validateEqual: true})
    } else {
      return null
    }
  }
  
  static match(ac: AbstractControl) {
    return new PasswordValidator().validate(ac);
  }
}
/**
 * Example <app-password [password]='changeMe' (passwordChange)='onChange($event)'/>
 */
@Component({
  selector: 'nmaas-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css']
})
export class PasswordComponent implements OnInit {
  public passwordForm: FormGroup;

  @Input()
  public password: string;

  @Output()
  public passwordSubmit = new EventEmitter<string>();

  constructor(private fb: FormBuilder) {
    this.passwordForm = fb.group(
      {
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required]
      },
      {
        validator: PasswordValidator.match
      })
  }

  ngOnInit() {
    this.passwordForm.controls['password'].setValue(this.password);
    this.passwordForm.controls['confirmPassword'].setValue(this.password);
  }

  public submit(): void {
    if(this.passwordForm.valid){
        this.passwordSubmit.emit(this.passwordForm.controls['password'].value);
        this.passwordForm.reset();
    }
  }
}
