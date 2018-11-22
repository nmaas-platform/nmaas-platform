import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import {ConfigurationService, UserService} from "../../service";
import {Configuration} from "../../model/configuration";
import {ShibbolethService} from "../../service/shibboleth.service";
import {ShibbolethConfig} from "../../model/shibboleth";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ModalComponent} from "../../shared/modal";

@Component({
  selector: 'nmaas-login',
  templateUrl: './login.component.html',
  styleUrls: [ './login.component.css' ],
  encapsulation: ViewEncapsulation.Emulated
})
export class LoginComponent implements OnInit {
    model: any = {};
    loading: boolean = false;
    error:string = '';
    configuration:Configuration;
    shibboleth:ShibbolethConfig;
    resetPassword:boolean = false;
    resetPasswordForm:FormGroup;

    @ViewChild(ModalComponent)
    public modal:ModalComponent;
  

    ssoLoading: boolean = false;
    ssoError:string = '';

    constructor(private router: Router, private auth: AuthService, private configService:ConfigurationService,
                private shibbolethService:ShibbolethService, private fb:FormBuilder, private userService:UserService) {
        this.resetPasswordForm = fb.group({
            email: ['', [Validators.required, Validators.email]]
        })
    }

    ngOnInit() {
        this.configService.getConfiguration().subscribe(config=>{
            this.configuration = config;
            if(config.ssoLoginAllowed){
                this.shibbolethService.getOne().subscribe(shibboleth => {
                    this.shibboleth = shibboleth;
                    this.checkSSO();
                });
            }
        });
    }

    public login(): void {
      this.loading = true;
      this.error = '';
      this.auth.login(this.model.username, this.model.password)
        .subscribe(result => {
          this.loading = false;
          this.router.navigate(['/']);
        }, err => {
          this.loading = false;
          this.error = err;
        });
    }


    public checkSSO() {
     let params = this.router.parseUrl(this.router.url).queryParams;

      if('ssoUserId' in params) {
        // Got auth data, send to api
        this.ssoLoading = true;
        this.ssoError = '';
        this.auth.propagateSSOLogin(params.ssoUserId)
            .subscribe(result => {
                if (result === true) {
                    this.ssoLoading = false;
                    this.router.navigate(['/']);
                } else {
                    this.ssoError = 'Failed to propagate SSO user id';
                    this.ssoLoading = false;
                }
            },
                err => {
                    console.error('Unable to propagate SSO user id. ' + err);
                    this.ssoLoading = false;
                    this.ssoError = err;
                });
      }
    }

  public triggerSSO() {
        let url = window.location.href.replace(/ssoUserId=.+/, '');
        window.location.href = this.shibboleth.loginUrl + '?return=' + url;
  }

  public sendResetNotification(){
      if(this.resetPasswordForm.valid){
          this.userService.resetPasswordNotification(this.resetPasswordForm.controls['email'].value).subscribe(() => {
              this.modal.show();
          }, err=>{
              this.modal.show();
          });
      }
  }
}
