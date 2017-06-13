import { Component, OnInit, ViewEncapsulation } from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { FooterComponent } from '../shared/index';

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
  
    
    constructor(private router: Router, private auth: AuthService) { }

    ngOnInit() {
        this.auth.logout();
    }

    public login():void {
        this.loading = true;
        this.error = '';
        this.auth.login(this.model.username, this.model.password)
            .subscribe(result => {
                if (result == true) {
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
}
