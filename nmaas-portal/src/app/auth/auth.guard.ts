import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from './auth.service';
import {ConfigurationService} from '../service';

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router, private maintenanceService: ConfigurationService) {}


  public canActivate(): boolean {
    this.maintenanceService.getConfiguration().subscribe(value => {
        if (this.auth.isLogged() && !this.auth.hasRole('ROLE_SUPERADMIN') && value.maintenance) {
            this.auth.logout();
            this.router.navigate(['/welcome/login']);
            return false;
        }
    });

    if (this.auth.isLogged()) {
      return true;
    }

    // not logged in so redirect to login page
    this.router.navigate(['/welcome/login']);
    return false;
  }

  public canActivateChild(): boolean {
    return this.canActivate();
  }
}
