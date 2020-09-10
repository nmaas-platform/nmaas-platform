import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {Domain} from '../../../model/domain';
import {DomainService} from '../../../service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {isUndefined} from 'util';
import {NG_VALIDATORS, PatternValidator} from '@angular/forms';
import {User} from '../../../model';
import {AppsService, UserService} from '../../../service';
import {Observable, of} from 'rxjs';
import {UserRole} from '../../../model/userrole';
import {CacheService} from '../../../service';
import {AuthService} from '../../../auth/auth.service';
import {ModalComponent} from '../../../shared/modal';
import {map, shareReplay, take} from 'rxjs/operators';
import {DcnDeploymentType} from '../../../model/dcndeploymenttype';
import {CustomerNetwork} from '../../../model/customernetwork';
import {MinLengthDirective} from '../../../directive/min-length.directive';
import {MaxLengthDirective} from '../../../directive/max-length.directive';


@Component({
    selector: 'app-domain',
    templateUrl: './domain.component.html',
    styleUrls: ['./domain.component.css'],
    providers: [
        {provide: NG_VALIDATORS, useExisting: PatternValidator, multi: true},
        {provide: NG_VALIDATORS, useExisting: MinLengthDirective, multi: true},
        {provide: NG_VALIDATORS, useExisting: MaxLengthDirective, multi: true}
    ]
})


export class DomainComponent extends BaseComponent implements OnInit {

    private domainId: number;
    public domain: Domain;
    public dcnUpdated = false;
    private domainUsers: User[];
    protected domainCache: CacheService<number, Domain> = new CacheService<number, Domain>();
    private keys: any = Object.keys(DcnDeploymentType).filter((type) => {
        return isNaN(Number(type));
    });

    @ViewChild(ModalComponent, { static: true })
    public modal: ModalComponent;

    constructor(public domainService: DomainService,
                protected userService: UserService,
                private router: Router,
                private route: ActivatedRoute,
                private location: Location,
                private authService: AuthService,
                protected appsService: AppsService) {
        super();
    }

    ngOnInit() {
        this.modal.setModalType('warning');
        this.modal.setStatusOfIcons(true);
        this.mode = this.getMode(this.route);
        this.route.params.subscribe(params => {
            if (!isUndefined(params['id'])) {
                this.domainId = +params['id'];
                this.domainService.getOne(this.domainId).subscribe(
                    (domain: Domain) => {
                        this.domain = domain;
                    },
                    err => {
                        console.error(err);
                        if (err.statusCode && (err.statusCode === 404 ||
                            err.statusCode === 401 || err.statusCode === 403 || err.statusCode === 500)) {
                            this.router.navigateByUrl('/notfound');
                        }
                    });
            } else {
                this.domain = new Domain();
                this.domain.active = true;
            }
            if (!this.authService.hasRole('ROLE_OPERATOR')) {
                let users: Observable<User[]>;
                users = this.userService.getAll(this.domainId);

                users.subscribe((all) => {
                    this.domainUsers = all;
                });
            }
        });
    }

    protected submit(): void {
        if (!isUndefined(this.domainId)) {
            this.updateExistingDomain();
        } else {
            this.domainService.add(this.domain).subscribe(() => this.router.navigate(['admin/domains/']));
        }
        this.domainService.setUpdateRequiredFlag(true);
    }

    public updateExistingDomain(): void {
        this.authService.hasRole('ROLE_SYSTEM_ADMIN') ? this.domainService.update(this.domain).subscribe(
            () => this.handleDcnConfiguration()) : this.domainService.updateTechDetails(this.domain).subscribe(
            () => this.handleDcnConfiguration());
    }

    public handleDcnConfiguration(): void {
        if (this.dcnUpdated && this.isManual()) {
            this.modal.show();
        } else {
            this.router.navigate(['admin/domains/']);
        }
    }

    public updateDcnConfigured(): void {
        this.domainService.updateDcnConfigured(this.domain).subscribe(() => {
            this.modal.hide();
            this.router.navigate(['admin/domains/']);
        });
    }

    public changeDcnFieldUpdatedFlag(): void {
        this.dcnUpdated = !this.dcnUpdated;
    }

    protected getDomainRoleNames(roles: UserRole[]): UserRole[] {
        const domainRoles: UserRole[] = [];
        roles.forEach((value => {
            if (value.domainId === this.domainId) {
                domainRoles.push(value);
            }
        }));
        return domainRoles;
    }

    protected getDomainName(domainId: number): Observable<string> {
        if (this.domainCache.hasData(domainId)) {
            return of(this.domainCache.getData(domainId).codename);
        } else {
            return this.domainService.getOne(domainId).pipe(
                map((domain) => {
                    this.domainCache.setData(domainId, domain);
                    return domain.codename
                }),
                shareReplay(1),
                take(1));
        }
    }

    protected filterDomainNames(user: User): UserRole[] {
        return user.roles.filter(role => role.domainId !== this.domainService.getGlobalDomainId() || role.role.toString() !== 'ROLE_GUEST');
    }

    public isManual(): boolean {
        return this.domain.domainDcnDetails.dcnDeploymentType === 'MANUAL';
    }

    public removeNetwork(index: number) {
        this.domain.domainDcnDetails.customerNetworks.splice(index, 1);
    }

    public addNetwork() {
        this.domain.domainDcnDetails.customerNetworks.push(new CustomerNetwork());
    }
}
