import {Component, OnInit} from '@angular/core';

import {isUndefined} from 'util';

import {AppInstance, AppInstanceState} from '../../../model/index';
import {DomainService} from '../../../service/domain.service';
import {AppInstanceService, AppsService} from '../../../service/index';
import {AuthService} from '../../../auth/auth.service';
import {AppConfigService} from '../../../service/appconfig.service';
import {UserDataService} from '../../../service/userdata.service';
import {Observable} from 'rxjs/Observable';
import {NgxPaginationModule} from 'ngx-pagination';
import {CustomerSearchCriteria} from "../../../service/index";

export enum AppInstanceListSelection {
  ALL, MY,
};

@Component({
  selector: 'nmaas-appinstancelist',
  templateUrl: './appinstancelist.component.html',
  styleUrls: ['./appinstancelist.component.css'],
  providers: [AppInstanceService, AppsService, DomainService, AuthService, NgxPaginationModule]
})
export class AppInstanceListComponent implements OnInit {

  public maxItemsOnPage: number = 5;
  public pageNumber: number = 1;

  public showFailed: boolean = true;

  public itemsPerPage: number[]  = [5,10,15,20,25,30];

  public AppInstanceState: typeof AppInstanceState = AppInstanceState;
  public AppInstanceListSelection: typeof AppInstanceListSelection = AppInstanceListSelection;

  public appInstances: Observable<AppInstance[]>;

  public listSelection: AppInstanceListSelection = AppInstanceListSelection.MY;

  public selectedUsername: string;
  public domainId: number = 0;

  constructor(private appInstanceService: AppInstanceService,
              private domainService: DomainService,
              private userDataService: UserDataService,
              private authService: AuthService,
              private appConfig: AppConfigService) {
  }

  ngOnInit() {
    this.userDataService.selectedDomainId.subscribe(domainId => this.update(domainId));

  }

  public update(domainId: number): void {
    if (isUndefined(domainId) || domainId === 0 || domainId === this.appConfig.getNmaasGlobalDomainId()) {
      this.domainId = undefined;
    } else {
      this.domainId = domainId;
    }
    this.getInstances({sortColumn: 'name', sortDirection:'asc'})
  }

  public checkPrivileges(app) {
    return app.owner.username === this.authService.getUsername() || this.authService.hasRole('ROLE_SYSTEM_ADMIN') || this.authService.hasDomainRole(app.domainId, 'ROLE_DOMAIN_ADMIN');
  }

  public onSelectionChange(event) {
    this.update(this.domainId);
  }

  public setItems(item){
    //console.log("Max items per page: " + this.maxItemsOnPage.toString() + " -> " + item.toString())
    this.maxItemsOnPage = item;
  }

  onSorted($event){
    this.getInstances($event)
  }

  getInstances(criteria: CustomerSearchCriteria){
    //console.log("Change to: " + criteria.sortColumn.toString() + " and " + criteria.sortDirection.toString())
    switch (+this.listSelection) {
      case AppInstanceListSelection.ALL:
        this.appInstances = this.appInstanceService.getSortedAllAppInstances(this.domainId, criteria);
        break;
      case AppInstanceListSelection.MY:
        this.appInstances = this.appInstanceService.getSortedMyAppInstances(this.domainId, criteria);
        break;
      default:
        this.appInstances = Observable.of<AppInstance[]>([]);
        break;
    }
  }

  public setShowFailedField(status: boolean){
    this.showFailed = status;
  }
}
