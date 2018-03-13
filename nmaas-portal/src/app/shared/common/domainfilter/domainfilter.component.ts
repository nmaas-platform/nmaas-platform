import {AuthService} from '../../../auth/auth.service';
import { Domain } from '../../../model/domain';
import { AppConfigService } from '../../../service/appconfig.service';
import {DomainService} from '../../../service/domain.service';
import {UserDataService} from '../../../service/userdata.service';
import {Component, OnInit, Input, OnDestroy} from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/interval';
import { isUndefined, isNullOrUndefined } from 'util';

@Component({
  selector: 'nmaas-domain-filter',
  templateUrl: './domainfilter.component.html',
  styleUrls: ['./domainfilter.component.css'],
})
export class DomainFilterComponent implements OnInit, OnDestroy {

  //@Input()
  public domainId: number;

  protected domains: Observable<Domain[]>
  
  protected refresh: Subscription;

  constructor(protected authService: AuthService, protected domainService: DomainService, protected userData: UserDataService, protected appConfig: AppConfigService) {}

  ngOnInit() {
    
    this.refresh = Observable.interval(10000).subscribe(() => this.updateDomains()); 
    
    this.userData.selectedDomainId.subscribe(id => this.domainId = id);
    this.updateDomains();
    
  }

  protected updateDomains(): void {
    if (this.authService.hasRole('ROLE_SUPERADMIN')) {
      this.domains = this.domainService.getAll();
    } else {
      this.domains = this.domainService.getMyDomains();
    }
    if(!isUndefined(this.domains)) {      
      this.domains = this.domains.map((domains) => domains.filter((domain) => domain.id !== this.appConfig.getNmaasGlobalDomainId()));
    }
  }
  
  ngOnDestroy(): void {
    if(!isNullOrUndefined(this.refresh)) {
      this.refresh.unsubscribe();
    }
  }  
  
  public onChange($event) {
    console.log('onChange(domainId)');
    this.userData.selectDomainId(Number(this.domainId));
  }

}
