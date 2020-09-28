import {AppConfigService} from '../../../service';
import {AppSubscriptionsService} from '../../../service/appsubscriptions.service';
import {CacheService} from '../../../service';
import {UserDataService} from '../../../service/userdata.service';
import {ListType} from '../../common/listtype';
import {AppViewType} from '../../common/viewtype';
import {Component, OnInit, Input, ViewEncapsulation} from '@angular/core';
import {Observable} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {AppDescription} from '../../../model/app-description';
import {Domain} from '../../../model/domain';
import {ApplicationBase} from '../../../model/application-base';

@Component({
  selector: 'nmaas-applist',
  templateUrl: './applist.component.html',
  styleUrls: ['./applist.component.css'],
  encapsulation: ViewEncapsulation.None,
})

export class AppListComponent implements OnInit {

  public ListType = ListType;
  public AppViewType = AppViewType;

  public cache: CacheService<number, boolean> = new CacheService();

  @Input()
  public appView: AppViewType = AppViewType.APPLICATION;

  @Input()
  public listType: ListType;

  @Input()
  public applications: Observable<ApplicationBase[]>;

  @Input()
  public selected: Observable<Set<number>>;

  @Input()
  public domainId: number;

  @Input()
  public domain: Observable<Domain>;

  public domainObject: Domain = undefined;


  constructor(private appSubscriptionService: AppSubscriptionsService,
              private userDataService: UserDataService,
              private appConfig: AppConfigService,
              private translate: TranslateService) {
    if (this.listType === undefined) {
      this.listType = ListType.GRID;
    }
    if (this.appView === undefined) {
      this.appView = AppViewType.APPLICATION;
    }
  }

  ngOnInit() {
    this.domain.subscribe(data => {
      this.domainObject = data;
    });
  }

  public getDescription(app: ApplicationBase): AppDescription {
    return app.descriptions.find(val => val.language === this.translate.currentLang);
  }

  public getAppTags(app: ApplicationBase): string {
    return app.tags.map(t => t.name).join(', ')
  }

}
