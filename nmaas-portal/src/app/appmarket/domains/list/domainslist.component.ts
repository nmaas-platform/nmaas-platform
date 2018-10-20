import {AuthService} from '../../../auth/auth.service';
import {Domain} from '../../../model/domain';
import {Role} from '../../../model/userrole';
import {DomainService} from '../../../service/domain.service';
import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-domains-list', templateUrl: './domainslist.component.html', styleUrls: ['./domainslist.component.css']
})
export class DomainsListComponent implements OnInit {

  public domains: Observable<Domain[]>;

  constructor(protected domainService: DomainService, protected authService: AuthService, private translate: TranslateService) {
    const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }

  ngOnInit() {
    this.update();
  }

  protected update(): void {
    if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN]) || this.authService.hasRole(Role[Role.ROLE_OPERATOR])) {
      this.domains = this.domainService.getAll().map((domains) => domains.filter((domain) => domain.id !== this.domainService.getGlobalDomainId()));
    } else {
      this.domains = this.domainService.getAll().map((domains) => domains.filter((domain) => this.authService.hasDomainRole(domain.id, Role[Role.ROLE_DOMAIN_ADMIN])));
    }
  }

  public remove(domainId: number): void {
    this.domainService.remove(domainId).subscribe(() => this.update());
    this.domainService.setUpdateRequiredFlag(true);
  }

}
