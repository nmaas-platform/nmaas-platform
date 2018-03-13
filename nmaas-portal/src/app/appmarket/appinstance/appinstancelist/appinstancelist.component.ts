import {Domain} from '../../../model/domain';
import {Component, OnInit} from '@angular/core';

import {isUndefined} from 'util';

import {AppInstance, AppInstanceState} from '../../../model/index';
import {DomainService} from '../../../service/domain.service';
import {AppsService, AppInstanceService} from '../../../service/index';
import {AuthService} from '../../../auth/auth.service';
import {AppConfigService} from '../../../service/appconfig.service';
import {UserDataService} from '../../../service/userdata.service';
import { Observable } from 'rxjs/Observable';

export enum AppInstanceListSelection {
  ALL,
  MY,
};

@Component({
  selector: 'nmaas-appinstancelist',
  templateUrl: './appinstancelist.component.html',
  styleUrls: ['./appinstancelist.component.css'],
  providers: [AppInstanceService, AppsService, DomainService, AuthService]
})
export class AppInstanceListComponent implements OnInit {

  private AppInstanceState: typeof AppInstanceState = AppInstanceState;
  private AppInstanceListSelection: typeof AppInstanceListSelection = AppInstanceListSelection;

  private appInstances: Observable<AppInstance[]>;

  private listSelection: AppInstanceListSelection = AppInstanceListSelection.MY;

  private selectedUsername: string;
  private domainId: number = 0;

  constructor(private appInstanceService: AppInstanceService, private domainService: DomainService, private userDataService: UserDataService, private authService: AuthService, private appConfig: AppConfigService) {}

  ngOnInit() {
    this.userDataService.selectedDomainId.subscribe(domainId => this.update(domainId));

  }

  protected update(domainId: number): void {
    if (isUndefined(domainId) || domainId === 0 || domainId === this.appConfig.getNmaasGlobalDomainId()) {
      this.domainId = undefined;
    } else {
      this.domainId = domainId;
    }

    switch (+this.listSelection) {
      case AppInstanceListSelection.ALL:
        this.appInstances = this.appInstanceService.getAllAppInstances(this.domainId);
        break;
      case AppInstanceListSelection.MY:
        this.appInstances = this.appInstanceService.getMyAppInstances(this.domainId);
        break;
      default:
        this.appInstances = Observable.of<AppInstance[]>([]);
        break;
    }

  }

  protected onSelectionChange(event) {
    this.update(this.domainId);
  }


}
