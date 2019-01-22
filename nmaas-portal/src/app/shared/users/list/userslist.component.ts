import {Domain} from '../../../model/domain';
import {User} from '../../../model/user';
import {CacheService} from '../../../service/cache.service';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {Observable, of} from 'rxjs';


import { isUndefined } from 'util';
import {Role, UserRole} from '../../../model/userrole';
import {UserDataService} from "../../../service/userdata.service";
import {AuthService} from "../../../auth/auth.service";
import {map, shareReplay, take} from 'rxjs/operators';

@Component({
  selector: 'nmaas-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent extends BaseComponent implements OnInit, OnChanges {

  @Input()
  public users: User[] = [];

  public domainId: number;

  @Output()
  public onDelete: EventEmitter<User> = new EventEmitter<User>();

  @Output()
  public onView: EventEmitter<number> = new EventEmitter<number>();

  public domainCache: CacheService<number, Domain> = new CacheService<number, Domain>();

  constructor(private userService: UserService,
              public domainService: DomainService,
              private userDataService: UserDataService,
              private authService: AuthService) {
    super();
    userDataService.selectedDomainId.subscribe(domain => this.domainId = domain);
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('UsersList:onChanges ' + changes.toString());
    this.userDataService.selectedDomainId.subscribe(domain => this.domainId = domain);
  }
  public getDomainName(domainId: number): Observable<string> {
    //console.debug('getDomainName(' + domainId + ')');
    if (this.domainCache.hasData(domainId)) {
      //console.debug('getDomainName(' + domainId + ') from cache');
      return of(this.domainCache.getData(domainId).name);
    } else {
      //console.debug('getDomainName(' + domainId + ') from network');
      return this.domainService.getOne(domainId).pipe(
          map((domain) => {this.domainCache.setData(domainId, domain); return domain.name}),
          shareReplay(1),
          take(1));
    }
  }

  public filterDomainNames(user: User): UserRole[]{
    return user.roles.filter(role => role.domainId != this.domainService.getGlobalDomainId());
  }

  public getOnlyDomainRoles(user: User): UserRole[]{
    return user.roles.filter(role => role.domainId===this.domainId);
  }

  public getGlobalRole(user: User): string{
    let userRole: UserRole[] = user.roles.filter(role => role.domainId === this.domainService.getGlobalDomainId());
    return userRole[0].role.toString() === Role[Role.ROLE_GUEST] ?'-' : userRole[0].role.toString().slice(5);
  }

  public getUserDomainIds(user: User): number[] {
    if (!isUndefined(user)) {
      return user.getDomainIds();
    } else {
      return [];
    }
  }

  public remove(user: User) {
    this.onDelete.emit(user);
  }

  public view(userId: number): void {
    console.debug('view(' + userId + ')');
    this.onView.emit(userId);
  }

    public changeUserStatus(user: User, enabled: boolean): void {
      this.userService.changeUserStatus(user.id, enabled).subscribe();
      user.enabled = enabled;
    }

}
