import {Application} from '../../../model/application';
import {AppSubscription} from '../../../model/appsubscription';
import {AppConfigService} from '../../../service/appconfig.service';
import {AppSubscriptionsService} from '../../../service/appsubscriptions.service';
import {CacheService} from '../../../service/cache.service';
import {UserDataService} from '../../../service/userdata.service';
import {ListTypeAware, ListType} from '../../common/listtype';
import {AppViewType, AppViewTypeAware} from '../../common/viewtype';
import {Component, OnInit, Input, ViewEncapsulation, OnDestroy} from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import {Observable} from 'rxjs/Observable';
import {isUndefined} from 'util';

@Component({
  selector: 'nmaas-applist',
  templateUrl: './applist.component.html',
  styleUrls: ['./applist.component.css'],
  encapsulation: ViewEncapsulation.None,
})
@ListTypeAware
@AppViewTypeAware
export class AppListComponent implements OnInit, OnDestroy {


  protected cache: CacheService<number, boolean> = new CacheService();

  @Input()
  public appView: AppViewType = AppViewType.APPLICATION;

  @Input()
  public listType: ListType;

  @Input()
  public applications: Observable<Application[]>;
  
  @Input()
  public selected: Map<number, boolean>;

  constructor(private appSubscriptionService: AppSubscriptionsService, private userDataService: UserDataService, private appConfig: AppConfigService) {
    if (isUndefined(this.listType)) {
      this.listType = ListType.GRID;
    }
  }

  ngOnInit() {
    
  }

  ngOnDestroy(): void {
  }

  
  public isAppSubscribed(appId?: number): boolean {
    //console.debug('isAppSubscribed:' + appId);
    /*
    if (isUndefined(appId) || isUndefined(this.domainId)) {
      return false;
    }
    if (this.cache.hasData(appId)) {
      return this.cache.getData(appId);
    } else {
      this.cache.setData(appId, false);

      if (this.domainId === 0) {
        console.debug('getAllByApplication: ' + appId);
        this.appSubscriptionService.getAllByApplication(appId).subscribe((result) => this.cache.setData(appId, result.length > 0), (error) => this.cache.setData(appId, false));
      } else {
        console.debug('getSubscription for: ' + appId);
        this.appSubscriptionService.getSubscription(appId, this.domainId).subscribe((result) => this.cache.setData(appId, true), (error) => this.cache.setData(appId, false));
      }

      return false;
    }
    */
    return false;
  }

}
