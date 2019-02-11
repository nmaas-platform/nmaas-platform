import {Application} from '../../../model/application';
import {AppConfigService} from '../../../service/appconfig.service';
import {AppSubscriptionsService} from '../../../service/appsubscriptions.service';
import {CacheService} from '../../../service/cache.service';
import {UserDataService} from '../../../service/userdata.service';
import {ListType} from '../../common/listtype';
import {AppViewType} from '../../common/viewtype';
import {Component, OnInit, Input, ViewEncapsulation, OnDestroy} from '@angular/core';
import {Observable} from 'rxjs';
import {isUndefined} from 'util';
import {TranslateService} from "@ngx-translate/core";
import {AppDescription} from "../../../model/appdescription";

@Component({
  selector: 'nmaas-applist',
  templateUrl: './applist.component.html',
  styleUrls: ['./applist.component.css'],
  encapsulation: ViewEncapsulation.None,
})

export class AppListComponent implements OnInit, OnDestroy {

  public ListType = ListType;
  public AppViewType = AppViewType;

  public cache: CacheService<number, boolean> = new CacheService();

  @Input()
  public appView: AppViewType = AppViewType.APPLICATION;

  @Input()
  public listType: ListType;

  @Input()
  public applications: Observable<Application[]>;
  
  @Input()
  public selected: Observable<Set<number>>;

  @Input()
  public domainId: number;


  constructor(private appSubscriptionService: AppSubscriptionsService, private userDataService: UserDataService,
              private appConfig: AppConfigService, private translate:TranslateService) {
    if (isUndefined(this.listType)) {
      this.listType = ListType.GRID;
    }
    if(isUndefined(this.appView)) {
      this.appView = AppViewType.APPLICATION;
    }
  }

  ngOnInit() {
    
  }

  ngOnDestroy(): void {
  }

  public getDescription(app:Application): AppDescription {
    return app.descriptions.find(val => val.language == this.translate.currentLang);
  }

}
