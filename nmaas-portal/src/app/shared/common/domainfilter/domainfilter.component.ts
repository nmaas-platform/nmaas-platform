import {AuthService} from '../../../auth/auth.service';
import { Domain } from '../../../model/domain';
import { AppConfigService } from '../../../service/appconfig.service';
import {DomainService} from '../../../service/domain.service';
import {UserDataService} from '../../../service/userdata.service';
import {Component, OnInit, Input, OnDestroy} from '@angular/core';
import { Subscription ,  Observable } from 'rxjs';

import { isUndefined, isNullOrUndefined } from 'util';
import {map} from 'rxjs/operators';
import {interval} from 'rxjs/internal/observable/interval';

@Component({
  selector: 'nmaas-domain-filter',
  templateUrl: './domainfilter.component.html',
  styleUrls: ['./domainfilter.component.css'],
})
export class DomainFilterComponent implements OnInit, OnDestroy {

  //@Input()
  public domainId: number;

  public domains: Observable<Domain[]>;

  public refresh: Subscription;

  constructor(protected authService: AuthService, protected domainService: DomainService, protected userData: UserDataService, protected appConfig: AppConfigService) {}

  ngOnInit() {
      if(this.authService.hasRole('ROLE_SYSTEM_ADMIN')){
        this.refresh = interval(10000).subscribe(next => {
            if(this.domainService.shouldUpdate()) {
                this.updateDomains();
                this.domainService.setUpdateRequiredFlag(false);
            }
        });
      }
      this.updateDomains();
      this.domains.subscribe(domain => this.userData.selectDomainId(domain[0].id));
      this.userData.selectedDomainId.subscribe(id => this.domainId = id);
  }

  public updateDomains(): void {
    if (this.authService.hasRole('ROLE_SYSTEM_ADMIN')) {
      this.domains = this.domainService.getAll();
    } else {
      this.domains = this.domainService.getMyDomains();
      if(!isUndefined(this.domains) && !this.authService.hasDomainRole(this.appConfig.getNmaasGlobalDomainId(),'ROLE_TOOL_MANAGER') && !this.authService.hasDomainRole(this.appConfig.getNmaasGlobalDomainId(), 'ROLE_OPERATOR')) {
           this.domains = this.domains.pipe(
               map((domains) => domains.filter((domain) => domain.id !== this.appConfig.getNmaasGlobalDomainId())));
      }
    }
  }
  
  ngOnDestroy(): void {

  }
  
  public onChange($event) {
    console.log('onChange(',this.domainId,')');
    this.userData.selectDomainId(Number(this.domainId));
  }

}
