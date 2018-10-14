import {Component, OnInit} from '@angular/core';

import {isUndefined} from 'util';

import {AppInstance, AppInstanceState} from '../../../model/index';
import {DomainService} from '../../../service/domain.service';
import {AppInstanceService, AppsService} from '../../../service/index';
import {AuthService} from '../../../auth/auth.service';
import {AppConfigService} from '../../../service/appconfig.service';
import {UserDataService} from '../../../service/userdata.service';
import {Observable} from 'rxjs/Observable';
import {TranslateService} from '@ngx-translate/core';

export enum AppInstanceListSelection {
  ALL, MY,
};

@Component({
  selector: 'nmaas-appinstancelist',
  templateUrl: './appinstancelist.component.html',
  styleUrls: ['./appinstancelist.component.css'],
  providers: [AppInstanceService, AppsService, DomainService, AuthService]
})
export class AppInstanceListComponent implements OnInit {

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
              private appConfig: AppConfigService,
              private translate: TranslateService) {
    const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
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

  public checkPrivileges(app) {
    return app.owner.username === this.authService.getUsername() || this.authService.hasRole('ROLE_SUPERADMIN') || this.authService.hasDomainRole(app.domainId, 'ROLE_DOMAIN_ADMIN');
  }

  public onSelectionChange(event) {
    this.update(this.domainId);
  }

}
