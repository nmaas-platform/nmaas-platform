import {AuthService} from '../../../auth/auth.service';
import {User} from '../../../model/user';
import {Role} from '../../../model/userrole';
import {DomainService} from '../../../service/domain.service';
import {JsonMapperService} from '../../../service/jsonmapper.service';
import {UserService} from '../../../service/user.service';
import {Component, OnInit} from '@angular/core';
import {ComponentMode, ComponentModeAware} from '../../../shared/common/componentmode';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';

@Component({
  selector: 'app-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
@ComponentModeAware
export class UsersListComponent implements OnInit {

  private allUsers: User[] = [];
  private domainUsers: Map<number, User[]> = new Map<number, User[]>();

  constructor(protected authService: AuthService,
    protected userService: UserService,
    protected domainService: DomainService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location) {}


  ngOnInit() {
    if (this.authService.hasRole(Role[Role.ROLE_SUPERADMIN])) {
      this.userService.getAll().subscribe((users) => {
        this.allUsers = users;
      });
    } else if (this.authService.hasRole(Role[Role.ROLE_DOMAIN_ADMIN])) {
      const userDomainIds: number[] = this.authService.getDomainsWithRole(Role[Role.ROLE_DOMAIN_ADMIN]);

      for (let i = 0; i < userDomainIds.length; i++) {
        this.userService.getAll(userDomainIds[i]).subscribe((users) => this.domainUsers.set(i, users));
      }
    }
  }

  public onUserView($event): void {
    console.debug('userId:' + $event);
    this.router.navigate(['/users/view/', $event]);
  }

  public onUserDelete($event): void {
    this.userService.deleteOne($event);
  }
  
}
