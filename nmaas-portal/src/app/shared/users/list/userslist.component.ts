import {Domain} from '../../../model/domain';
import {User} from '../../../model/user';
import {CacheService} from '../../../service/cache.service';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';
import {Observable, of} from 'rxjs';

import { isUndefined } from 'util';
import {UserRole} from '../../../model/userrole';
import {UserDataService} from "../../../service/userdata.service";
import {AuthService} from "../../../auth/auth.service";
import {map, shareReplay, take} from 'rxjs/operators';
import {CustomerSearchCriteria} from "../../../service";

@Component({
  selector: 'nmaas-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent extends BaseComponent implements OnInit, OnChanges {

  readonly users_item_number_key = "NUMBER_OF_USERS_ITEM_KEY";

  @Input()
  public users: User[] = [];

  public domainId: number;

  @Output()
  public onDelete: EventEmitter<User> = new EventEmitter<User>();

  @Output()
  public onView: EventEmitter<number> = new EventEmitter<number>();

  public domainCache: CacheService<number, Domain> = new CacheService<number, Domain>();

  private lastSearchCriteria: CustomerSearchCriteria = undefined;

  private pageNumber = 1;
  private paginatorName = 'paginator-identifier';
  public itemsPerPage: number[]  = [5, 10, 15, 20, 25, 30];
  public maxItemsOnPage: number = 5;

  constructor(private userService: UserService,
              public domainService: DomainService,
              private userDataService: UserDataService,
              private authService: AuthService) {
    super();
    userDataService.selectedDomainId.subscribe(domain => this.domainId = domain);
  }

  ngOnInit() {
    // set stored value of maxElementsPerPage
    let i = sessionStorage.getItem(this.users_item_number_key);
    if(i) this.maxItemsOnPage = +i;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.userDataService.selectedDomainId.subscribe(domain => this.domainId = domain);
  }
  public getDomainName(domainId: number): Observable<string> {
    if (this.domainCache.hasData(domainId)) {
      return of(this.domainCache.getData(domainId).name);
    } else {
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
    return userRole[0].role.toString();
  }

  public getUserDomainIds(user: User): number[] {
    if (!isUndefined(user)) {
      return user.getDomainIds();
    } else {
      return [];
    }
  }

  public getLastSuccessfulLoginDateString(user: User): string {
    return user.lastSuccessfulLoginDate != null ? user.lastSuccessfulLoginDate.toUTCString() : '';
  }

  public getFirstLoginDateString(user: User): string {
    return user.firstLoginDate != null ? user.firstLoginDate.toUTCString() : '';
  }

  public remove(user: User) {
    this.onDelete.emit(user);
  }

  public view(userId: number): void {
    this.onView.emit(userId);
  }

  public changeUserStatus(user: User, enabled: boolean): void {
    this.userService.changeUserStatus(user.id, enabled).subscribe();
    user.enabled = enabled;
    // sort after changing params
    if(this.lastSearchCriteria) this.handleSortEvent(this.lastSearchCriteria);
  }

  onSorted($event) {
    console.log($event);
    this.handleSortEvent($event);
  }

  handleSortEvent(criteria: CustomerSearchCriteria) {
    this.lastSearchCriteria = criteria;
    const baseSortFunc = (a: any, b: any): number => {
      if(a < b) return -1;
      if(a > b) return  1;
      return 0;
    };

    this.users.sort(
        (a: User, b: User) => {
          const direction = criteria.sortDirection === 'asc' ? 1 : -1;
          let result = 0;

          // sorting rules for custom columns
          if(criteria.sortColumn === 'domains') {
            const ad = this.filterDomainNames(a);
            const bd = this.filterDomainNames(b);
            if (!ad) console.log(ad);
            if (!bd) console.log(bd);
            const ar = ad.length > 0 ? ad[0].domainId : 0;
            const br = bd.length > 0 ? bd[0].domainId : 0;
            result = baseSortFunc(ar, br);
          }
          else if (criteria.sortColumn === 'globalRole') {
            result = baseSortFunc(this.getGlobalRole(a) , this.getGlobalRole(b))
          }
          else if (criteria.sortColumn === 'roles') {
            const ad = this.getOnlyDomainRoles(a);
            const bd = this.getOnlyDomainRoles(b);
            const ar = ad.length > 0 ? ad[0].role.toString() : '';
            const br = bd.length > 0 ? bd[0].role.toString() : '';
            result = baseSortFunc(ar, br);
          } // default sorting rule
          else {
            result = baseSortFunc(a[criteria.sortColumn], b[criteria.sortColumn]);
          }

          return result * direction;
        }
    )
  }

  public setItems(item){
    // store max items per page value in this session
    sessionStorage.setItem(this.users_item_number_key, item);
    this.maxItemsOnPage = item;
  }

}
