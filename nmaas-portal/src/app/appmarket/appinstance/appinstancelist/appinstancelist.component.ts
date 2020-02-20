import {Component, OnInit} from '@angular/core';

import {isUndefined} from 'util';

import {AppInstance, AppInstanceState} from '../../../model';
import {DomainService} from '../../../service';
import {AppInstanceService, AppsService, CustomerSearchCriteria} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {AppConfigService} from '../../../service';
import {UserDataService} from '../../../service/userdata.service';
import {Observable, of} from 'rxjs';
import {NgxPaginationModule} from 'ngx-pagination';
import {TranslateService} from "@ngx-translate/core";
import {map} from 'rxjs/operators';
import {TranslateStateModule} from "../../../shared/translate-state/translate-state.module";
import {SessionService} from "../../../service/session.service";
import {Domain} from "../../../model/domain";

export enum AppInstanceListSelection {
  ALL, MY,
}

@Component({
  selector: 'nmaas-appinstancelist',
  templateUrl: './appinstancelist.component.html',
  styleUrls: ['./appinstancelist.component.css'],
  providers: [AppInstanceService, AppsService, DomainService, AuthService, NgxPaginationModule]
})
export class AppInstanceListComponent implements OnInit {

  public undeployedVisible = false;

  private readonly item_number_key: string = 'item_number_per_page';

  public p_first: string = 'p_first';
  public p_second: string = 'p_second';

  public maxItemsOnPage: number = 5;
  public maxItemsOnPageSec: number = 5;

  public pageNumber: number = 1;
  public secondPageNumber: number = 1;

  public showFailed: boolean = true;

  public itemsPerPage: number[]  = [5, 10, 15, 20, 25, 30];

  public AppInstanceState: typeof AppInstanceState = AppInstanceState;
  public AppInstanceListSelection: typeof AppInstanceListSelection = AppInstanceListSelection;

  public appInstances: Observable<AppInstance[]>;
  public appDeployedInstances: Observable<AppInstance[]>;
  public appUndeployedInstances: Observable<AppInstance[]>;

  public listSelection: AppInstanceListSelection = AppInstanceListSelection.MY;

  public selectedUsername: string;
  public domainId: number = 0;

  public domains: Domain[] = [];

  constructor(private appInstanceService: AppInstanceService,
              private domainService: DomainService,
              private userDataService: UserDataService,
              public authService: AuthService,
              private appConfig: AppConfigService,
              private translateService: TranslateService,
              private sessionService: SessionService,
              private translateState: TranslateStateModule) {
  }

  ngOnInit() {
    this.sessionService.registerCulture(this.translateService.currentLang);
    this.userDataService.selectedDomainId.subscribe(domainId => this.update(domainId));
    this.domainService.getAll().subscribe(result => {
      this.domains.push(...result);
    });
    let i = sessionStorage.getItem(this.item_number_key);
    if (i) {
      this.maxItemsOnPage = +i;
      this.maxItemsOnPageSec = +i;
    }
  }

  public getDomainNameById(id: number): string {
    if(this.domains === undefined){
      return 'none';
    }
    return this.domains.find(value => value.id === id).name;
  }

  public translateEnum(value: AppInstanceListSelection): string{
    let outValue = '';
    if (value.toString() === 'ALL') {
      this.translateService.get('ENUM.ALL').subscribe((res: string) => {
        outValue = res;
      })
    }
    if (value.toString() === 'MY') {
      this.translateService.get('ENUM.MY').subscribe((res: string) => {
        outValue = res;
      })
    }
    return outValue;
  }

  public update(domainId: number): void {
    if (isUndefined(domainId) || domainId === 0 || domainId === this.appConfig.getNmaasGlobalDomainId()) {
      this.domainId = this.appConfig.getNmaasGlobalDomainId();
    } else {
      this.domainId = domainId;
    }
    this.getInstances({sortColumn: 'createdAt', sortDirection: 'asc'})
  }

  public checkPrivileges(app) {
    return app.owner.username === this.authService.getUsername()
        || this.authService.hasRole('ROLE_SYSTEM_ADMIN')
        || this.authService.hasDomainRole(app.domainId, 'ROLE_DOMAIN_ADMIN');
  }

  public onSelectionChange(event) {
    this.update(this.domainId);
  }

  public setItems(item){
    sessionStorage.setItem(this.item_number_key, item);
    this.maxItemsOnPage = item;
    this.maxItemsOnPageSec = item;
  }

  onSorted($event) {
    this.getInstances($event)
  }

  getInstances(criteria: CustomerSearchCriteria){
    console.debug('Crit: ', criteria);
    this.appInstances = of<AppInstance[]>([]);
    switch (+this.listSelection) {
      case AppInstanceListSelection.ALL:
        if (this.domainId) {
          this.appInstances = this.appInstanceService.getSortedAllAppInstances(this.domainId, criteria);
        }
        break;
      case AppInstanceListSelection.MY:
        if (this.domainId) {
          this.appInstances = this.appInstanceService.getSortedMyAppInstances(this.domainId, criteria);
        }
        break;
      default:
        break;
    }
    this.appInstances = this.appInstances.pipe(
        map( app => app.filter(
            (appInst) => (this.domainId == this.appConfig.getNmaasGlobalDomainId() || this.domainId == appInst.domainId)
        ))
    );
    // sort and filter deployed instances
    this.appDeployedInstances = this.appInstances.pipe(
        map(AppInstances => AppInstances.filter(
        app => (AppInstanceState[app.state] != AppInstanceState.REMOVED.toString()
          && AppInstanceState[app.state] != AppInstanceState.DONE.toString()
      ))));
    // sort and filter undeployed instances
    this.appUndeployedInstances = this.appInstances.pipe(
        map(AppInstances => AppInstances.filter(
        app => (AppInstanceState[app.state] == AppInstanceState.REMOVED.toString()
          || AppInstanceState[app.state] == AppInstanceState.DONE.toString()
        ))));
  }


  public setShowFailedField(status: boolean) {
    this.showFailed = status;
  }
}
