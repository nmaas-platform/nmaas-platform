import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from './auth.service';
import {MaintenanceService} from "../service";

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router, private maintenanceService:MaintenanceService) {}

  public maintenance:boolean = false;

  public canActivate(): boolean {
    this.maintenanceService.getMaintenance().subscribe(value => this.maintenance = value.maintenance);
    if (this.auth.isLogged() && (!this.maintenance || this.auth.hasRole('ROLE_SUPERADMIN'))) {
      return true;
    }

    // not logged in or portal maintenance period is active so redirect to login page
    this.router.navigate(['/welcome/login']);
    return false;
  }

  public canActivateChild(): boolean {
    return this.canActivate();
  }
}
