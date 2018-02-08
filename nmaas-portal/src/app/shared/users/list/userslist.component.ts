import {Domain} from '../../../model/domain';
import {User} from '../../../model/user';
import {CacheService} from '../../../service/cache.service';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {Observable} from 'rxjs/Observable';




@Component({
  selector: 'nmaas-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent extends BaseComponent implements OnInit, OnChanges {

  @Input()
  users: User[];

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
    if (this.domainCache.hasData(domainId)) {
      return Observable.of(this.domainCache.getData(domainId).name);
    } else {
      return this.domainService.getOne(domainId).map((domain) => {this.domainCache.setData(domainId, domain); return domain.name});
    }
  }

  protected remove(userId: number) {
    this.onDelete.emit(userId);
  }

  protected view(userId: number): void {
    this.onView.emit(userId);
  }
}
