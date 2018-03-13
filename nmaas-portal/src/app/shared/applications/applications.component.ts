import {Application} from '../../model/application';
import {AppSubscription} from '../../model/appsubscription';
import {AppConfigService} from '../../service/appconfig.service';
import {AppsService} from '../../service/apps.service';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {ListTypeAware, ListType} from '../common/listtype';
import {AppViewType, AppViewTypeAware} from '../common/viewtype';
import {Component, OnInit, Input, OnDestroy, OnChanges, SimpleChanges} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {isUndefined} from 'util';

@Component({
  selector: 'nmaas-applications-view',
  templateUrl: './applications.component.html',
  styleUrls: ['./applications.component.css']
})
@ListTypeAware
@AppViewTypeAware
export class ApplicationsViewComponent implements OnInit, OnChanges, OnDestroy {

  @Input()
  public appView: AppViewType = AppViewType.APPLICATION;

  @Input()
  public selectedListType: ListType = ListType.GRID;

  @Input()
  public domainId: number;

  protected applications: Observable<Application[]>;
  protected selected: Map<number, boolean>;

  protected searchedAppName: string;

  constructor(private appsService: AppsService, private appSubsService: AppSubscriptionsService, private userDataService: UserDataService, private appConfig: AppConfigService) {}

  ngOnInit() {
    this.updateDomain();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.updateDomain();
  }

  protected updateDomain(): void {

    let domainId: number;
    let applications: Observable<Application[]>;

    if (isUndefined(this.domainId) || this.domainId === 0 || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
      domainId = undefined;
    } else {
      domainId = this.domainId;
    }

    switch (+this.appView) {
      case AppViewType.APPLICATION:
        applications = this.appsService.getApps();
        break;
      case AppViewType.DOMAIN:
        applications = this.appSubsService.getSubscribedApplications(domainId);
        break;
      default:
        applications = Observable.of<Application[]>([]);
        break;
    }

    this.applications = applications;

  }

  ngOnDestroy(): void {

  }

  public filterAppsByName($event): void {

  }

}
