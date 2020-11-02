import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';

import {Router} from '@angular/router';
import {AuthService} from '../../auth/auth.service';
import {ConfigurationService, UserService} from '../../service';
import {Configuration} from '../../model/configuration';
import {ShibbolethService} from '../../service/shibboleth.service';
import {ShibbolethConfig} from '../../model/shibboleth';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ModalComponent} from '../../shared/modal';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'nmaas-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    encapsulation: ViewEncapsulation.Emulated
})
export class LoginComponent implements OnInit {
    model: any = {};
    loading = false;
    error = '';
    configuration: Configuration;
    shibboleth: ShibbolethConfig;
    resetPassword = false;
    resetPasswordForm: FormGroup;

    @ViewChild(ModalComponent, {static: true})
    public modal: ModalComponent;

    ssoLoading = false;
    ssoError = '';

    constructor(private router: Router,
                private auth: AuthService,
                private configService: ConfigurationService,
                private shibbolethService: ShibbolethService,
                private fb: FormBuilder,
                private userService: UserService,
                private translate: TranslateService) {
        this.resetPasswordForm = fb.group({
            email: ['', [Validators.required, Validators.email]]
        });
    }

    ngOnInit() {
        this.configService.getConfiguration().subscribe(config => {
            this.configuration = config;
            if (config.ssoLoginAllowed) {
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
        this.auth.login(this.model.username, this.model.password).subscribe(
            () => {
                this.loading = false;
                this.translate.setDefaultLang(this.auth.getSelectedLanguage());
                this.translate.use(this.auth.getSelectedLanguage());
                this.router.navigate(['/']);
            }, err => {
                this.error = this.translate.instant(this.getMessage(err));
                this.loading = false;
            }
        );
    }


    public checkSSO() {
        const params = this.router.parseUrl(this.router.url).queryParams;

        if ('ssoUserId' in params) {
            // Got auth data, send to api
            this.ssoLoading = true;
            this.ssoError = '';
            this.auth.propagateSSOLogin(params.ssoUserId).subscribe(
                result => {
                    if (result === true) {
                        this.ssoLoading = false;
                        this.translate.setDefaultLang(this.auth.getSelectedLanguage());
                        this.translate.use(this.auth.getSelectedLanguage());
                        this.router.navigate(['/']);
                    } else {
                        this.ssoError = 'Failed to propagate SSO user id';
                        this.ssoLoading = false;
                    }
                },
                err => {
                    this.ssoError = this.translate.instant(this.getMessage(err));
                    this.ssoLoading = false;
                }
            );
        }
    }

    public triggerSSO() {
        const url = window.location.href.replace(/ssoUserId=.+/, '');
        window.location.href = this.shibboleth.loginUrl + '?return=' + url;
    }

    public sendResetNotification() {
        if (this.resetPasswordForm.valid) {
            this.userService.resetPasswordNotification(this.resetPasswordForm.controls['email'].value).subscribe(
                () => {
                    this.modal.show();
                }, () => {
                    this.modal.show();
                }
            );
        }
    }

    private getMessage(err: any): string {
        switch (err['status']) {
            case 401:
                return this.ssoLoading ? 'LOGIN.USER_DISABLED_MESSAGE' : 'LOGIN.LOGIN_FAILURE_MESSAGE';
            case 406:
                return 'LOGIN.APPLICATION_UNDER_MAINTENANCE_MESSAGE';
            case 409:
                return 'GENERIC_MESSAGE.UNAVAILABLE_MESSAGE';
            default:
                return 'GENERIC_MESSAGE.UNAVAILABLE_MESSAGE';
        }
    }
}
