import { Component, OnInit, ViewEncapsulation } from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { FooterComponent } from '../../shared/index';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {ConfigurationService} from "../../service";
import {Configuration} from "../../model/configuration";

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
  

    ssoLoading: boolean = false;
    ssoError:string = '';

    constructor(private router: Router, private auth: AuthService, private configService:ConfigurationService) { }

    ngOnInit() {
        this.configService.getConfiguration().subscribe(config=>{
            this.configuration = config;
            if(this.auth.getUsername() && config.allowsSSO) {
                window.location.href = this.auth.getSSOLogoutUrl();
                this.auth.logout();
                return;
            }
            this.auth.logout();
            if(config.allowsSSO) {
                this.checkSSO();
            }
        });
    }

    public login():void {
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
                    this.error = 'Username or password is incorrect';
                    this.loading = false;
                }
            },
                err => {
                    console.error('Unable to login. ' + err);
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

      } else if(!this.configuration.allowsSSO) {
        this.triggerSSO();
      }
    }

  public triggerSSO() {
    // Need to start login process
    let url = window.location.href.replace(/ssoUserId=.+/, '');
    // Shibboleth SP uses parameter 'target' instead of 'return'
    window.location.href = this.auth.getSSOLoginUrl() + '?return=' + url;
  }}
