import {AuthService} from '../../../auth/auth.service';
import {Domain} from '../../../model/domain';
import {Role} from '../../../model/userrole';
import {DomainService} from '../../../service/domain.service';
import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-domains-list', templateUrl: './domainslist.component.html', styleUrls: ['./domainslist.component.css']
})
export class DomainsListComponent implements OnInit {

  public domains: Observable<Domain[]>;

  constructor(protected domainService: DomainService, protected authService: AuthService, public translate: TranslateService) {
  }

  ngOnInit() {
    this.update();
  }

  protected update(): void {
    if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN]) || this.authService.hasRole(Role[Role.ROLE_OPERATOR])) {
      this.domains = this.domainService.getAll().pipe(
          map((domains) => domains.filter((domain) => domain.id !== this.domainService.getGlobalDomainId())));
    } else {
      this.domains = this.domainService.getAll().pipe(
          map((domains) => domains.filter((domain) => this.authService.hasDomainRole(domain.id, Role[Role.ROLE_DOMAIN_ADMIN]))));
    }
  }

  public changeState(domain: Domain): void {
    this.domainService.updateDomainState(domain).subscribe(() => this.update());
    this.domainService.setUpdateRequiredFlag(true);
  }

  public getStateLabel(active: boolean) : string {
    return active ? this.translate.instant("DOMAINS.DISABLE_BUTTON") : this.translate.instant("DOMAINS.ENABLE_BUTTON");
  }

}
