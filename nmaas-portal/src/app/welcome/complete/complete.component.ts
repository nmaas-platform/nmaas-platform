import {Component, OnInit, ViewChild} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {Domain} from "../../model/domain";
import {RegistrationService} from "../../auth/registration.service";
import {UserService} from "../../service";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {Router} from "@angular/router";
import {AuthService} from "../../auth/auth.service";
import {ModalComponent} from "../../shared/modal";
import {isNullOrUndefined} from "util";

@Component({
    selector: 'app-complete',
    templateUrl: './complete.component.html',
    styleUrls: ['./complete.component.css'],
    providers:[ProfileService]
})

export class CompleteComponent extends BaseComponent implements OnInit {

    public user:User;
    public registrationForm: FormGroup;
    public domains: Observable<Domain[]>;
    public errorMessage: string = '';
    public sending: boolean = false;
    public submitted: boolean = false;
    public success: boolean = false;

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    constructor(private fb: FormBuilder,
                private registrationService: RegistrationService,
                protected profileService:ProfileService,
                private userService: UserService,
                private authService: AuthService,
                private router: Router) {
        super();
        this.registrationForm = fb.group(
            {
                username: ['', [Validators.required, Validators.minLength(3)]],
                email: ['', [Validators.required, Validators.email]],
                firstname: [''],
                lastname: ['']
            });
    }

    ngOnInit() {
        this.profileService.getOne().subscribe((user)=>this.user = user)
    }

    public onSubmit(): void {
        if (this.registrationForm.valid) {
            this.user.enabled = false;
            this.user.username = this.registrationForm.controls['username'].value;
            this.user.email = this.registrationForm.controls['email'].value;
            this.user.firstname = this.registrationForm.controls['firstname'].value;
            this.user.lastname = this.registrationForm.controls['lastname'].value;

            this.userService.completeRegistration(this.user).subscribe(
                (result) => {
                    console.log("Data updated successfully.");
                    this.success = true;
                    this.authService.logout();
                    this.modal.show();
                },
                    (err) => {
                        console.log("Unable to finish user registration");
                        this.sending = false;
                        this.submitted = true;
                        this.success = false;
                        this.errorMessage = isNullOrUndefined(err.message)?'Service is unavailable. Please try again later':'Invalid input data';
                    },
                    () => {
                        this.sending = false;
                        this.submitted = true;
                    }
            );



        }
    }

    public hide(): void{
        this.router.navigate(['/']);
    }

}
