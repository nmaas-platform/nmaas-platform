import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthService} from './auth.service';
import {ConfigurationService} from '../service';

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router, private maintenanceService: ConfigurationService) {}


  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.auth.isLogged()) {
      this.maintenanceService.getConfiguration().subscribe(value => {
         if (!this.auth.hasRole('ROLE_SYSTEM_ADMIN') && value.maintenance) {
             this.auth.logout();
             this.router.navigate(['/welcome/login']);
             return false;
         }
      });
      if(this.auth.hasRole('ROLE_INCOMPLETE') && route.url.toString() !== 'complete') {
          this.router.navigate(['/complete']);
          return false;
      }
      if(this.auth.hasRole("ROLE_NOT_ACCEPTED") && route.url.toString() !== 'terms-acceptance'){
          this.router.navigate(['/terms-acceptance']);
          return false;
      }
      else
          return true;
    }

    // not logged in so redirect to login page
    this.router.navigate(['/welcome/login']);
    return false;
  }

  public canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.canActivate(route, state);
  }
}
