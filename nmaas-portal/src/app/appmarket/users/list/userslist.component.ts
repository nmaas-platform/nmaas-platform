import {AuthService} from '../../../auth/auth.service';
import {User} from '../../../model/user';
import {Role, UserRole} from '../../../model/userrole';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {UserDataService} from '../../../service/userdata.service';
import {Component, OnInit} from '@angular/core';
import {ComponentMode} from '../../../shared/common/componentmode';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
import {Observable, of} from 'rxjs';
import {isNullOrUndefined} from 'util';

@Component({
  selector: 'app-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent implements OnInit {

  public ComponentMode = ComponentMode;

  private domainId: number;

  private allUsers: User[] = [];
  //  private domainUsers: Map<number, User[]> = new Map<number, User[]>();

  constructor(protected authService: AuthService,
    protected userService: UserService,
    protected domainService: DomainService,
    protected userDataService: UserDataService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location) {}


  ngOnInit() {
    this.userDataService.selectedDomainId.subscribe((domainId) => this.update(domainId));
  }

  public update(domainId: number): void {
    console.log('Update users for domainId=' + domainId);
    if (isNullOrUndefined(domainId) || domainId === 0) {
      this.domainId = undefined;
    } else
      this.domainId = domainId;

    let users: Observable<User[]> = null;

    if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN])) {      
      users = this.userService.getAll(this.domainId);
    } else if (!isNullOrUndefined(this.domainId) && this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_DOMAIN_ADMIN])) {
      users = this.userService.getAll(this.domainId);
    } else {
      users = of<User[]>([]);
    }
    
    users.subscribe((all) => {
      this.allUsers = all;
      /* parse date strings to date objects */
      for(let u of this.allUsers) {
        if(u.firstLoginDate) {
          u.firstLoginDate = new Date(u.firstLoginDate)
        }
        if(u.lastSuccessfulLoginDate) {
          u.lastSuccessfulLoginDate = new Date(u.lastSuccessfulLoginDate)
        }
      }
    });

  }

  public onUserView($event): void {
    this.router.navigate(['/admin/users/view/', $event]);
  }

  public onUserDelete($event): void {
      this.userService.removeRole($event.id, $event.roles.find(value => value.domainId===this.domainId).role,this.domainId).subscribe(()=> this.update(this.domainId))
  }

}
