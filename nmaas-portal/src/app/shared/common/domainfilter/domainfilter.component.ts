import {AuthService} from '../../../auth/auth.service';
import {Domain} from '../../../model/domain';
import {DomainService} from '../../../service';
import {UserDataService} from '../../../service/userdata.service';
import {Component, OnInit} from '@angular/core';
import {Subscription, Observable, of, interval} from 'rxjs';

import {map} from 'rxjs/operators';
import {ProfileService} from '../../../service/profile.service';
import {User} from '../../../model';

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

    public profile: User;

    constructor(private authService: AuthService,
                private domainService: DomainService,
                private userData: UserDataService,
                private profileService: ProfileService) {
    }

    ngOnInit() {
        if (this.authService.hasRole('ROLE_SYSTEM_ADMIN')) {
            this.refresh = interval(10000).subscribe(next => {
                if (this.domainService.shouldUpdate()) {
                    this.updateDomains();
                    this.domainService.setUpdateRequiredFlag(false);
                }
            });
        }
        this.profileService.getOne().subscribe(
            profile => {
                this.profile = profile;

                this.updateDomains();
                this.domains.subscribe(domain => {
                    this.domainName = domain[0].name;
                    this.userData.selectDomainId(domain[0].id)
                });
            }
        );

        this.userData.selectedDomainId.subscribe(id => this.domainId = id);
    }

    public updateDomains(): void {
        if (this.authService.hasRole('ROLE_SYSTEM_ADMIN')) {
            this.domains = this.domainService.getAll();
        } else {
            this.domains = this.domainService.getMyDomains();
            const globalDomainId = this.domainService.getGlobalDomainId();
            if (this.domains === undefined) {
                this.domains = of([]);
            }
            if (!this.authService.hasDomainRole(globalDomainId, 'ROLE_TOOL_MANAGER')
                && !this.authService.hasDomainRole(globalDomainId, 'ROLE_OPERATOR')) {
                this.filterOutGlobalDomain();
                this.filterOutNotActiveDomains();
            }
        }
        this.sortDomains();
    }

    private filterOutNotActiveDomains(): void {
        this.domains = this.domains.pipe(
            map(
                (domains) => domains.filter(domain => domain.active)
            )
        );
    }

    private filterOutGlobalDomain(): void {
        const globalDomainId = this.domainService.getGlobalDomainId();
        this.domains = this.domains.pipe(
            map(
                (domains) => domains.filter(domain => domain.id !== globalDomainId)
            )
        );
    }

    private sortDomains(): void {
        const globalDomainId = this.domainService.getGlobalDomainId();
        this.domains = this.domains.pipe(
            map(
                domains => {
                    const global = domains.find(domain => domain.id === globalDomainId);
                    const defaultDomain = domains.find(domain => domain.id === this.profile.defaultDomain);
                    domains = domains.filter(domain => domain.id !== globalDomainId && domain.id !== this.profile.defaultDomain);

                    domains.sort((a: Domain, b: Domain): number => {
                        return a.name.localeCompare(b.name)
                    })
                    if (global !== undefined) {
                        domains.unshift(global)
                    }
                    if (defaultDomain !== undefined && this.profile.defaultDomain !== globalDomainId) {
                        domains.unshift(defaultDomain)
                    }
                    return domains
                }
            )
        )
    }

    public changeDomain(domainId: number, domainName: string) {
        console.log(`domainChange(${domainId})`);
        this.domainId = domainId;
        this.domainName = domainName;
        this.userData.selectDomainId(Number(domainId));
    }

    public getCurrent() {
        return this.domainName;
    }

}
