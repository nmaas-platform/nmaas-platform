import {Component, OnInit} from '@angular/core';

import {isUndefined} from 'util';

import {AppInstance, AppInstanceState} from '../../../model/index';
import {DomainService} from '../../../service/domain.service';
import {AppInstanceService, AppsService} from '../../../service/index';
import {AuthService} from '../../../auth/auth.service';
import {AppConfigService} from '../../../service/appconfig.service';
import {UserDataService} from '../../../service/userdata.service';
import {Observable, of} from 'rxjs';
import {NgxPaginationModule} from 'ngx-pagination';
import {CustomerSearchCriteria} from "../../../service/index";
import {element} from "protractor";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {map} from 'rxjs/operators';

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

  public p_first: string = "p_first";
  public p_second: string = "p_second";

  public maxItemsOnPage: number = 5;
  public maxItemsOnPageSec: number = 5;
  public pageNumber: number = 1;

  public secondPageNumber: number = 1;

  public showFailed: boolean = true;

  public itemsPerPage: number[]  = [5,10,15,20,25,30];

  public AppInstanceState: typeof AppInstanceState = AppInstanceState;
  public AppInstanceListSelection: typeof AppInstanceListSelection = AppInstanceListSelection;

  public appInstances: Observable<AppInstance[]>;
  public appDeployedInstances: Observable<AppInstance[]>;
  public appUndeployedInstances: Observable<AppInstance[]>;

  public listSelection: AppInstanceListSelection = AppInstanceListSelection.MY;

  public selectedUsername: string;
  public domainId: number = 0;

  constructor(private appInstanceService: AppInstanceService,
              private domainService: DomainService,
              private userDataService: UserDataService,
              private authService: AuthService,
              private appConfig: AppConfigService,
              private translateService: TranslateService) {
  }

  ngOnInit() {
    this.userDataService.selectedDomainId.subscribe(domainId => this.update(domainId));

  }

  public translateEnum(value: AppInstanceListSelection): string{
    switch (this.translateService.currentLang) {
      case 'en':
        return value.toString();
      case 'de':
        if(value.toString() == 'ALL'){
          return 'Alles';
        }
        if(value.toString() == 'MY'){
          return 'Meine';
        }
        break;
      case 'fr':
        if(value.toString() == 'ALL'){
          return 'Tout';
        }
        if(value.toString() == 'MY'){
          return 'Mon';
        }
        break;
      case 'pl':
        if(value.toString() == 'ALL'){
          return 'Wszystkie';
        }
        if(value.toString() == 'MY'){
          return 'Moje';
        }
        break;
      default:
        return value.toString();
    }
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
    this.maxItemsOnPage = item;
    this.maxItemsOnPageSec = item;
  }

  onSorted($event){
    this.getInstances($event)

  }

  getInstances(criteria: CustomerSearchCriteria){
    switch (+this.listSelection) {
      case AppInstanceListSelection.ALL:
        this.appInstances = this.appInstanceService.getSortedAllAppInstances(this.domainId, criteria);
        break;
      case AppInstanceListSelection.MY:
        this.appInstances = this.appInstanceService.getSortedMyAppInstances(this.domainId, criteria);
        break;
      default:
        this.appInstances = of<AppInstance[]>([]);
        break;
    }
    this.appDeployedInstances = this.appInstances.pipe(
        map(AppInstances => AppInstances.filter(
      app => app.userFriendlyState != 'Undeployed')));
    this.appUndeployedInstances = this.appInstances.pipe(
        map(AppInstances => AppInstances.filter(
      app => app.userFriendlyState == 'Undeployed')));
  }


  public setShowFailedField(status: boolean){
    this.showFailed = status;
  }
}
