import { Component, OnInit, ViewEncapsulation } from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { FooterComponent } from '../../shared/index';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {ConfigurationService} from "../../service";
import {Configuration} from "../../model/configuration";
import {isNullOrUndefined} from "util";
import {ShibbolethService} from "../../service/shibboleth.service";
import {ShibbolethConfig} from "../../model/shibboleth";
import {TranslateService} from '@ngx-translate/core';

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
    ssoLoading : boolean = false;
    ssoError : string = '';
    loginFailureErrorMessage = '';

  constructor(private router: Router,
              private auth: AuthService,
              private configService: ConfigurationService,
              private shibbolethService: ShibbolethService,
              private translate: TranslateService) {
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

    public login():void {
        this.translate.get(['LOGIN.LOGIN_FAILURE_MESSAGE'])
        .subscribe((response: string) => {
          this.loginFailureErrorMessage = Object.values(response)[0];
          // this.loginFailureErrorMessage = response;
        });
        this.loading = true;
        this.error = '';
        this.auth.login(this.model.username, this.model.password)
            .subscribe(result => {
                if (result === true) {
                    console.log('User logged in');
                    this.loading = false;
                    this.router.navigate(['/']);
                } else {
                    console.error('Error during login');
                    this.error = (this.loginFailureErrorMessage === null ?
                      'Username or password is incorrect' : this.loginFailureErrorMessage);
                    this.loading = false;
                }
            },
                err => {
                    console.error('Unable to login. ' + err);
                    this.loading = false;
                    // this.error = err;
                    this.error = (this.loginFailureErrorMessage === null ?
                      err : this.loginFailureErrorMessage);
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
  }}
