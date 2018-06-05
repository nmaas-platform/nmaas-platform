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
  protected selected: Observable<Set<number>>;

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
        applications.subscribe((apps) => this.updateSelected(apps));
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

  protected updateSelected(apps: Application[]) {

    let subscriptions: Observable<AppSubscription[]>;
    if (isUndefined(this.domainId) || this.domainId === 0 || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
      subscriptions = this.appSubsService.getAll();
    } else {
      subscriptions = this.appSubsService.getAllByDomain(this.domainId);
    }
    
    subscriptions.subscribe((appSubs) => {

      const selected: Set<number> = new Set<number>();

      for (let i = 0; i < appSubs.length; i++) {
        selected.add(appSubs[i].applicationId);
      }
      
      this.selected = Observable.of<Set<number>>(selected);
    });

  }

  ngOnDestroy(): void {

  }

  protected filterAppsByName(typed: string): void {

    if(typed.length === 0){this.updateDomain()};    
    let filteredApps: Application[];
    this.applications.subscribe((apps) => {
      filteredApps = apps.filter(app => app.name.toLocaleLowerCase().indexOf(typed) > -1);
      console.log("Ausgefürt");
      console.log(filteredApps);
      this.applications = Observable.of(filteredApps);
    });
  }

}
