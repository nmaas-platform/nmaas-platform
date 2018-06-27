import {Domain} from '../../../model/domain';
import {User} from '../../../model/user';
import {CacheService} from '../../../service/cache.service';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/shareReplay';
import 'rxjs/add/operator/take';
import { isUndefined } from 'util';
import {UserRole} from "../../../model/userrole";




@Component({
  selector: 'nmaas-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent extends BaseComponent implements OnInit, OnChanges {

  @Input()
  public users: User[] = [];


  @Output()
  public onDelete: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  public onView: EventEmitter<number> = new EventEmitter<number>();

  public domainCache: CacheService<number, Domain> = new CacheService<number, Domain>();

  constructor(private userService: UserService, private domainService: DomainService) {
    super();
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('UsersList:onChanges ' + changes.toString());
  }
  
  public getDomainName(domainId: number): Observable<string> {
    //console.debug('getDomainName(' + domainId + ')');
    if (this.domainCache.hasData(domainId)) {
      //console.debug('getDomainName(' + domainId + ') from cache');
      return Observable.of(this.domainCache.getData(domainId).name);
    } else {
      //console.debug('getDomainName(' + domainId + ') from network');
      return this.domainService.getOne(domainId).map((domain) => {this.domainCache.setData(domainId, domain); return domain.name})
              .shareReplay(1).take(1);
    }
  }

  public filterDomainNames(user:User):UserRole[]{
    return user.roles.filter(role => role.domainId != this.domainService.getGlobalDomainId());
  }

  public getUserDomainIds(user: User): number[] {
    if (!isUndefined(user)) {
      return user.getDomainIds();
    } else {
      return [];
    }
  }

  public remove(userId: number) {
    this.onDelete.emit(userId);
  }

  public view(userId: number): void {
    console.debug('view(' + userId + ')');
    this.onView.emit(userId);
  }
}
