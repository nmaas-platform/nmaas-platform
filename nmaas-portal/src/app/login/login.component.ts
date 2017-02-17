import { Component, OnInit, ViewEncapsulation } from '@angular/core';

import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: [ './login.component.css' ],
  encapsulation: ViewEncapsulation.None,
})
export class LoginComponent implements OnInit {
  model: any = {};
  loading = false;
  error = '';
  
    
  constructor(private router: Router, private auth: AuthService) { }

  ngOnInit() {
      this.auth.logout();
  }

    login() {
        this.loading = true;
        this.auth.login(this.model.username, this.model.password)
            .subscribe(result => {
                if (result === true) {
                    this.router.navigate(['/']);
                } else {
                    this.error = 'Username or password is incorrect';
                    this.loading = false;
                }
            });
    }
}
