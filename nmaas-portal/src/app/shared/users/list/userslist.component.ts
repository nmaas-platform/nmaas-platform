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




@Component({
  selector: 'nmaas-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent extends BaseComponent implements OnInit, OnChanges {

  @Input()
  users: User[] = [];


  @Output()
  onDelete: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  onView: EventEmitter<number> = new EventEmitter<number>();

  protected domainCache: CacheService<number, Domain> = new CacheService<number, Domain>();

  constructor(private userService: UserService, private domainService: DomainService) {
    super();
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('UsersList:onChanges ' + changes);
  }
  
  protected getDomainName(domainId: number): Observable<string> {
    console.debug('getDomainName(' + domainId + ')');
    if (this.domainCache.hasData(domainId)) {
      console.debug('getDomainName(' + domainId + ') from cache');
      return Observable.of(this.domainCache.getData(domainId).name);
    } else {
      console.debug('getDomainName(' + domainId + ') from network');
      return this.domainService.getOne(domainId).map((domain) => {this.domainCache.setData(domainId, domain); return domain.name})
              .shareReplay(1).take(1);
    }
  }

  protected getUserDomainIds(user: User): number[] {
    if (!isUndefined(user)) {
      return user.getDomainIds();
    } else {
      return [];
    }
  }

  protected remove(userId: number) {
    this.onDelete.emit(userId);
  }

  protected view(userId: number): void {
    console.debug('view(' + userId + ')');
    this.onView.emit(userId);
  }
}
