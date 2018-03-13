import {AuthService} from '../../../auth/auth.service';
import {User} from '../../../model/user';
import {Role} from '../../../model/userrole';
import {DomainService} from '../../../service/domain.service';
import {JsonMapperService} from '../../../service/jsonmapper.service';
import {UserService} from '../../../service/user.service';
import {UserDataService} from '../../../service/userdata.service';
import {Component, OnInit} from '@angular/core';
import {ComponentMode, ComponentModeAware} from '../../../shared/common/componentmode';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
import {Observable} from 'rxjs/Observable';
import {isNullOrUndefined} from 'util';

@Component({
  selector: 'app-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
@ComponentModeAware
export class UsersListComponent implements OnInit {

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

    if (this.authService.hasRole(Role[Role.ROLE_SUPERADMIN])) {      
      users = this.userService.getAll(this.domainId);
    } else if (!isNullOrUndefined(this.domainId) && this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_DOMAIN_ADMIN])) {
      users = this.userService.getAll(this.domainId);
    } else {
      users = Observable.of<User[]>([]);
    }
    
    users.subscribe((all) => {
      this.allUsers = all;
    });

  }

  public onUserView($event): void {
    this.router.navigate(['/users/view/', $event]);
  }

  public onUserDelete($event): void {
    this.userService.deleteOne($event);
  }

}
