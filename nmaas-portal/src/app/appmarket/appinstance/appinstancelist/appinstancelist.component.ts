import {Domain} from '../../../model/domain';
import {Component, OnInit} from '@angular/core';

import {isUndefined} from 'util';

import {AppInstance, AppInstanceState} from '../../../model/index';
import {DomainService} from '../../../service/domain.service';
import {AppsService, AppInstanceService} from '../../../service/index';
import {AuthService} from '../../../auth/auth.service';

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

  private appInstances: AppInstance[];

  private listSelection: AppInstanceListSelection = AppInstanceListSelection.MY;

  private selectedUsername: string;
  private selectedDomainId: number = 0;

  private domains: Domain[];
  
  private allDomains: Domain[];
  private userDomains: Domain[];

  constructor(private appInstanceService: AppInstanceService, private domainService: DomainService, private authService: AuthService) {}

  ngOnInit() {
    this.selectedDomainId = this.domainService.getGlobalDomainId();

    this.domainService.getMyDomains().subscribe(
      (domains: Domain[]) => {
        this.userDomains = domains.filter(domain => domain.id !== this.domainService.getGlobalDomainId())
        this.update();
        console.debug('UserDomains: ', this.userDomains);
      }
    );

    this.domainService.getAll().subscribe(
      (domains: Domain[]) => {
         this.allDomains = domains.filter(domain => domain.id !== this.domainService.getGlobalDomainId())
         this.update();
         console.debug('AllDomains: ', this.allDomains);
      }
    );

    if (!this.listSelection) {
      this.listSelection = AppInstanceListSelection.MY;
    }

    this.update();
  }

  private update(): void {
    
     switch (+this.listSelection) {
      case AppInstanceListSelection.ALL: {
        if (!isUndefined(this.selectedDomainId)
          && this.selectedDomainId !== this.domainService.getGlobalDomainId()
          && this.selectedDomainId !== 0) {
          this.appInstanceService.getAllAppInstances(this.selectedDomainId).subscribe(appInstances => this.appInstances = appInstances);
        } else {
          this.selectedDomainId = 0;
          this.appInstanceService.getAllAppInstances().subscribe(appInstances => this.appInstances = appInstances);
        }
        this.domains = this.allDomains;
        break;
      }
      case AppInstanceListSelection.MY: {
        if (!isUndefined(this.selectedDomainId)
          && this.selectedDomainId !== this.domainService.getGlobalDomainId()
          && this.selectedDomainId !== 0) {
          this.appInstanceService.getMyAppInstances(this.selectedDomainId).subscribe(appInstances => this.appInstances = appInstances);
        } else {
          this.selectedDomainId = 0;
          this.appInstanceService.getMyAppInstances().subscribe(appInstances => this.appInstances = appInstances);
        }
        this.domains = this.userDomains;
        break;
      }         
    }
    console.debug('listSelection: ', this.listSelection);
    console.debug('domains: ', this.domains);

  }

  protected onSelectionChange(event) {
    console.debug('onSelectionChange()');
    this.update();
  }


}
