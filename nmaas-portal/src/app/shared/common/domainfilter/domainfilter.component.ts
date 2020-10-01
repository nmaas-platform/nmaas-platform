import {AuthService} from '../../../auth/auth.service';
import { Domain } from '../../../model/domain';
import {DomainService, AppConfigService} from '../../../service';
import {UserDataService} from '../../../service/userdata.service';
import {Component, OnInit} from '@angular/core';
import { Subscription ,  Observable } from 'rxjs';

import {map} from 'rxjs/operators';
import {interval} from 'rxjs/internal/observable/interval';

@Component({
  selector: 'nmaas-domain-filter',
  templateUrl: './domainfilter.component.html',
  styleUrls: ['./domainfilter.component.css'],
})
export class DomainFilterComponent implements OnInit {

  public domainId: number;

  public domainName: string;

  public domains: Observable<Domain[]>;

  public refresh: Subscription;

  constructor(protected authService: AuthService,
              protected domainService: DomainService,
              protected userData: UserDataService,
              protected appConfig: AppConfigService) {}

  ngOnInit() {
      if (this.authService.hasRole('ROLE_SYSTEM_ADMIN')) {
        this.refresh = interval(10000).subscribe(next => {
            if (this.domainService.shouldUpdate()) {
                this.updateDomains();
                this.domainService.setUpdateRequiredFlag(false);
            }
        });
      }
      this.updateDomains();
      this.domains.subscribe(domain => {
          this.domainName = domain[0].name;
          this.userData.selectDomainId(domain[0].id)
      });

      this.userData.selectedDomainId.subscribe(id => this.domainId = id);
  }

  public updateDomains(): void {
    if (this.authService.hasRole('ROLE_SYSTEM_ADMIN')) {
      this.domains = this.domainService.getAll();
    } else {
      this.domains = this.domainService.getMyDomains();
      const globalDomainId = this.appConfig.getNmaasGlobalDomainId();
      if (this.domains !== undefined
          && !this.authService.hasDomainRole(globalDomainId, 'ROLE_TOOL_MANAGER')
          && !this.authService.hasDomainRole(globalDomainId, 'ROLE_OPERATOR')) {
           this.domains = this.domains.pipe(
               map(
                   (domains) => domains.filter((domain) => domain.id !== globalDomainId && domain.active))
           );
      }
    }
  }

  public onChange($event) {
    console.log('onChange(', this.domainId, ')');
    this.userData.selectDomainId(Number(this.domainId));
  }

  public changeDomain(domain: number, dName: string) {
    console.log('domainChange(', domain, ')');
    this.domainId = domain;
    this.domainName = dName;
    this.userData.selectDomainId(Number(domain));
  }

  public getCurrent() {
    return this.domainName;
  }

}
